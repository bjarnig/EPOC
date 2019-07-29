
BGenChalk : BGen
{ 	
	var paramValues, <>sound, <>positions, reverb, buf, effectBus, <>isPlaying, preControl;
	
	*new { |id=0, description, duration=10, control, outBus=0, positions, values, sound, buf,load=1|
		^super.newCopyArgs(id, description, duration, control, outBus, nil, values, sound, positions).init(load);
	}
	
	init {|load=1|
		
		this.setDescription;
		this.isPlaying = 0;
		preControl = BControl.new; 
		if(this.control.isNil, {this.control = BControl.new});
		
		if(load > 0, { 
		Routine {
			if(sound.isNil, {this.sound = BConstants.monoSnd});
			if(positions.isNil, {this.positions = [0.85, 0.9, 0.95, 1, 0.95, 0.9, 0.85, 0.9]});
			buf = Buffer.read(Server.local, sound);
			Server.local.sync;
			this.initEffect.value; }.play;
		});
	}
	
	setParam {|paramName, paramValue| 
		if(paramName == \duration, {duration = paramValue});
		if(paramName == \sound, {this.sound = paramValue;});
		if(paramName == \positions, {this.positions = paramValue;});
		if(paramName == \outBus, {outBus = paramValue});
	}
	
	setParamAndUpdate {|param, value|
	if(this.isPlaying > 0,
	{control.setParamValue(param, value);
	this.update.value;})}	
	
	initEffect {
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\chalkVerb, [\in, effectBus, \out, outBus]);
		reverb.play;
	}
	
	*loadSynthDefs {
		
	 	SynthDef(\chalkGrain, 
		{|buf=0, outBus, rate=1, trate=12, grDur=0.5, pos=0.1, atk=4, sus=5, rel=6, amp=1, lopass=19000, hipass=80, entropy=0.0, pan=0.99999, dist=0.0, dvol=1.0, svol=1.0,
		positions = #[0.85, 0.9, 0.95, 1, 0.95, 0.9, 0.85, 0.9]|
		var clk, signal, env, filtEnv, distSignal, seq, trig, seqPos;
		
		clk = Impulse.kr(trate); 
		pos = pos * BufDur.kr(buf);
	 	seq = Dseq(pos * positions, inf);
		trig = Impulse.kr((15 * entropy) + 0.001);
		seqPos = Demand.kr(trig, 0, seq);
		env = EnvGen.ar(Env.new([0,1,1,0],[atk,sus,rel]), doneAction:2);
		filtEnv = EnvGen.ar(Env.new([0,1,1,0],[atk/4,sus,rel/1.2], [-8]), doneAction:2);
		signal = TGrains.ar(2, clk, buf, rate, seqPos, grDur, pan, 2.0);
		signal = ((((signal * env) * amp) * dvol) * svol);
		signal = LeakDC.ar(signal); 
		distSignal = HPF.ar(SoftClipAmp8.ar(signal, pregain:170) * 0.1, 8000) + LPF.ar(SoftClipAmp8.ar(signal, pregain:270) * 0.1, 100);
		signal = (signal * (1-dist))  + (distSignal * dist);
		signal = HPF.ar(signal, hipass);
		signal = LPF.ar(signal, lopass + ((20000 - lopass) * filtEnv));
		Out.ar(outBus, signal); 
		}).add; 
		
		SynthDef(\chalkVerb, {|in=0, out=0, mix=0.01, d1 = 0.80, d2 = 0.82, d3 = 0.84, d4 = 0.86, d5 = 0.88,
Ê Ê Ê	t1 = 2.1, t2 = 2.2, t3 = 2.3, t4 = 2.4, t5 = 2.5, f1 = 20000, f2 = 20000, f3 = 20000, f4 = 20000, 
		f5 = 20000, amp=1.0, delayMult=0.01, decayMult=8.0, filtMult=0.6|
Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê
Ê Ê Ê	var input, output, c1, c2, c3, c4, c5;
		input = In.ar(in, 2);
		
	  	c1 = LPF.ar(CombC.ar(input, 1, d1 * delayMult, t1 * decayMult), f1 * filtMult);
Ê Ê Ê Ê	c2 = LPF.ar(CombC.ar(input, 1, d2 * delayMult, t2 * decayMult), f2 * filtMult);
Ê Ê Ê Ê	c3 = LPF.ar(CombC.ar(input, 1, d3 * delayMult, t3 * decayMult), f3 * filtMult);
Ê Ê Ê Ê	c4 = LPF.ar(CombC.ar(input, 1, d4 * delayMult, t4 * decayMult), f4 * filtMult);
Ê Ê Ê Ê	c5 = LPF.ar(CombC.ar(input, 1, d5 * delayMult, t5 * decayMult), f5 * filtMult);
Ê Ê Ê Ê
Ê Ê Ê Ê	output = (((c1 + c2 + c3 + c4 + c5) * 0.4) * mix) + (input * (1.0-mix));
		Out.ar(out, output * amp);

	}).add;		
	}
	
	play {
	
	this.stop.value; 
	
	if(this.isPlaying == 0,  
	{
		this.playWrap.value;
		this.update(1).value;
		this.isPlaying = 1;
	}, 
	{ 
		this.update(0).value; });
	}
	
	playWrap {
	var atk, sus, rel;	
	
		atk = control.attack * duration;
		rel = control.release * duration;
		sus = duration - (atk + rel);
		wrap = Bwrap.new(\chalkGrain, paramValues);
		wrap.setValues([\outBus, effectBus, \buf, buf, \atk, atk, \sus, sus, \rel, rel, \amp, control.amplitude, \positions, positions]);
		('Chalk start'.postln);
		wrap.play;
	}
	
stop {arg release=0;
	var ampstep, delta, amp, steps;

		if(release > 0 && this.isPlaying == 1, {
		
		delta = 0.1;
		amp = wrap.get(\amp, amp);
		steps = release / delta;
		ampstep = amp / steps;
		
		Routine {
		steps.do {
		amp = amp - ampstep;
		if(wrap.synth.isPlaying, {wrap.set(\amp, amp);});
		delta.wait;
		};
		
		wrap.stop;		
		this.isPlaying = 0;
		}.play;
		
		}, {
		
		if(this.isPlaying == 1, {
		if(wrap.synth.isPlaying, {wrap.stop;});
		this.isPlaying = 0;});
		
		});
	}
	
	dispose {
		
		if(this.isPlaying == 1, {this.stop.value});
		this.freeEffect;
		this.isPlaying = 0;
	}
	
	update {arg updateAll=0;
	
	var trate, atk, sus, rel, amp, lopass, hipass, grDur;
	
	if(updateAll == 1 || (control.release != preControl.release) || (control.attack != preControl.attack), 
	{
		atk = control.attack * duration;
		rel = control.release * duration;
		sus = duration - (atk + rel);
		wrap.set(\atk, atk);
		wrap.set(\sus, sus);
		wrap.set(\rel, rel);
	});
	
	if(updateAll == 1 || (control.position != preControl.position), 
	{
		wrap.set(\pos, Env.new([0.05, 0.3, 0.6, 0.75, 1.0],[0.25, 0.25, 0.25, 0.25]).at(control.position));
	});

	if(updateAll == 1 || (control.location != preControl.location), 
	{
		wrap.set(\pan,  Env.new([-0.99999, 0.99999],[1.0]).at(control.location * 1.11111));
	});
	
	if(updateAll == 1 || (control.frequency != preControl.frequency), 
	{
		wrap.set(\rate, Env.new([0.0, 0.2, 1.0, 1.5, 2.0],[0.25, 0.25, 0.25, 0.25]).at(control.frequency));
		reverb.set(\delayMult, Env.new([8, 4, 2, 0.5, 0.25],[0.25, 0.25, 0.25, 0.25]).at(control.frequency));
	});
	
	if(updateAll == 1 || (control.speed != preControl.speed), 
	{
		wrap.set(\svol,  Env.new([1.2, 1.1, 1, 0.99, 0.91],[0.25, 0.25, 0.25, 0.25]).at(control.speed));
		trate = Env.new([0.001, 0.5, 2, 8, 20],[0.25, 0.25, 0.25, 0.25]).at(control.speed);
		wrap.set(\trate, trate);
	});
	
	if(updateAll == 1 || (control.density != preControl.density), 
	{
		wrap.set(\dvol,  Env.new([1.3, 1.1, 1, 0.95, 0.88],[0.25, 0.25, 0.25, 0.25]).at(control.density));
		wrap.set(\grDur,  Env.new([0.05, 0.25, 0.5, 2, 8.0],[0.25, 0.25, 0.25, 0.25]).at(control.density));
	});
	
	if(updateAll == 1 || (control.surface != preControl.surface), 
	{
		lopass = 19990 * (1.01 - control.surface);
		hipass = 400 * (1.01 - control.surface);
		if(lopass > 19999, {lopass = 19999});
		if(hipass < 25, {hipass = 25});
		wrap.set(\lopass, lopass); 
		wrap.set(\hipass, hipass);
		wrap.set(\dist, Env.new([0.0, 0.1, 0.2, 0.4, 0.5],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
		reverb.set(\filtMult, Env.new([0.1, 0.2, 0.5, 0.75, 0.95],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
	});
	
	if(updateAll == 1 || (control.amplitude != preControl.amplitude), 
	{
		wrap.set(\amp, Env.new([0.0, 0.25, 1.0, 3.0, 8.0],[0.25, 0.25, 0.25, 0.25]).at(control.amplitude));
	});
	
	if(updateAll == 1 || (control.entropy != preControl.entropy), 
	{
		wrap.set(\entropy, control.entropy);
	});
	
	if(updateAll == 1 || (control.color != preControl.color), 
	{
		reverb.set(\amp, Env.new([0.99, 1.0, 1.2, 1.4, 1.8],[0.25, 0.25, 0.25, 0.25]).at(control.color));
		reverb.set(\mix, Env.new([0.0, 0.2, 0.4, 0.6, 0.8],[0.25, 0.25, 0.25, 0.25]).at(control.color));
	});
	
	preControl.copy(this.control);
	
	}
	
	freeEffect {
		if(reverb.synth.isPlaying, {reverb.stop});
		if(effectBus.index.isNil.not, {effectBus.free}); 
	} 
	
	setDescription {
		description = "BGenChalk";
	}
}