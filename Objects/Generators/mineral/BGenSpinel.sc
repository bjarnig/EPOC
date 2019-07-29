
BGenSpinel : BGen
{ 	
	var paramValues, <>sound, reverb, buf, effectBus, <>isPlaying, preControl;
	
	*new { |id=0, description, duration=10, control, outBus=0, values, sound, buf,load=1|
		^super.newCopyArgs(id, description, duration, control, outBus, nil, values, sound).init(load);
	}
	
	init {|load=1|
		
		this.setDescription;
		this.isPlaying = 0;
		preControl = BControl.new; 
		if(this.control.isNil, {this.control = BControl.new});
		
		if(load > 0, { 
		Routine {
			if(sound.isNil, {this.sound = BConstants.stereoSnd});
			buf = Buffer.read(Server.local, sound);
			Server.local.sync;
			this.initEffect.value; }.play;
		});
	}
	
	setParam {|paramName, paramValue|
		if(paramName == \duration, {duration = paramValue});
		if(paramName == \sound, {this.sound = paramValue;});
		if(paramName == \outBus, {outBus = paramValue});
	}
	
	setParamAndUpdate {|param, value|
	if(this.isPlaying > 0,
	{control.setParamValue(param, value);
	this.update.value;})}	
	
	initEffect {
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\spinelVerb, [\inBus, effectBus, \outBus, outBus]);
		reverb.play;
	}
	
	*loadSynthDefs {
		
		SynthDef(\spinelGrain, { |outBus=0, atk=4, sus=5, rel=6, amp=1, startPos=0.0, density=0.5, buf=0, rate=1.0, randSpeed=10, entropy=0.5, speed=0.01, ampMod=0.5, 
		pan=1.0, bpFreq=1000, delLeft=0.02, delRight=0.02, ampLeft=0.5, ampRight=0.5|
		var env, totalFrames, loopsamp, loopdur, loopfreq, trig, trigs, egs, upOrDown, bufpos, sig, bufDur, start, end, outLeft, outRight; 
		env = EnvGen.ar(Env.new([0,1,1,0],[atk,sus,rel]), doneAction:2); 
		bufDur = (BufDur.ir(buf) * SampleRate.ir); 
		start = bufDur * startPos;
		end = start + ((bufDur - start) * density);
	 	loopsamp = end-start;
		loopdur = abs(loopsamp);
		loopfreq = (SampleRate.ir / loopdur);
		trig = Impulse.kr(loopfreq);
		trigs = PulseDivider.kr(trig, 2, (0..1));
		egs = EnvGen.kr(Env.linen(0.02, loopfreq.reciprocal - 0.02, 0.02), trigs);
		upOrDown = loopsamp.sign * rate;
		bufpos = Phasor.ar(trigs, upOrDown, start,
		Select.kr(upOrDown > 0, [0, BufFrames.ir(buf)]), start);
		sig = (BufRd.ar(2, buf, bufpos, loop: 0) * egs).sum;
		sig = BPeakEQ.ar(sig, bpFreq, 3, -9); 
		sig = ((sig * SinOsc.ar(speed + LFNoise2.ar(18*entropy, entropy, 0.0).max(0.0), 0.5, 0.5)) * ampMod) + (sig * (1-ampMod));
		sig = sig * env;
		sig = sig * amp;
		outLeft = sig[0] * ampLeft; 
 		outRight = sig[1] * ampRight; 
		Out.ar(outBus, [outLeft, outRight]); 
		}).add;
		
		SynthDef(\spinelVerb, 
		{arg outBus = 0, inBus=2, amp=1, mix=0.01, maxDelay=8, delay=0.9, feedback=0.80;
		var fx, sig, fxlevel, level;
		fxlevel = mix;
		level = 1.0 - fxlevel;
		sig = InFeedback.ar(inBus,2); 
		fx = sig + LocalIn.ar(2); 
		fx = (DelayC.ar(fx, maxDelay, delay) + LPF.ar(DelayC.ar(fx, maxDelay * 1.2, delay * 1.2), 200) + HPF.ar(DelayC.ar(fx, maxDelay * 0.8, delay * 0.8), 4000)) * 0.4; 
		LocalOut.ar(fx * feedback); 
Ê Ê		Out.ar(outBus, (fx * fxlevel) + (sig * level)) 
		}).add;	}
	
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
		wrap = Bwrap.new(\spinelGrain, paramValues);
		wrap.setValues([\outBus, effectBus, \buf, buf, \atk, atk, \sus, sus, \rel, rel, \amp, control.amplitude]);
		('Spinel start'.postln);
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
		if(wrap.synth.isPlaying, {wrap.stop});
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
		wrap.set(\startPos, Env.new([0.0, 0.3, 0.5, 0.7, 0.9],[0.25, 0.25, 0.25, 0.25]).at(control.position));
	});

	if(updateAll == 1 || (control.location != preControl.location), 
	{
		wrap.set(\ampLeft,  Env.new([0.6, 0.55, 0.5, 0.45, 0.4],[0.25, 0.25, 0.25, 0.25]).at(control.location));
		wrap.set(\ampRight,  Env.new([0.4, 0.45, 0.5, 0.55, 0.6],[0.25, 0.25, 0.25, 0.25]).at(control.location));
		wrap.set(\ampMod, Env.new([1.0, 0.75, 0.5, 0.25, 0.0],[0.25, 0.25, 0.25, 0.25]).at(control.location));
	});
	
	if(updateAll == 1 || (control.frequency != preControl.frequency), 
	{
		wrap.set(\rate, Env.new([0.5, 0.75, 1.0, 1.5, 2.5],[0.25, 0.25, 0.25, 0.25]).at(control.frequency));
	});
	
	if(updateAll == 1 || (control.speed != preControl.speed), 
	{	
		wrap.set(\speed,  Env.new([0.001, 0.05, 1.0, 1, 20],[0.25, 0.25, 0.25, 0.25]).at(control.speed));
		reverb.set(\delay,  Env.new([0.8, 0.6, 0.6, 0.2, 0.1],[0.25, 0.25, 0.25, 0.25]).at(control.speed));
	});
	
	if(updateAll == 1 || (control.density != preControl.density), 
	{
		wrap.set(\density,  Env.new([0.0, 0.25, 0.5, 0.75, 1.0],[0.25, 0.25, 0.25, 0.25]).at(control.density));
	});
	
	if(updateAll == 1 || (control.surface != preControl.surface), 
	{
		wrap.set(\bpFreq, Env.new([8000, 4000, 1000, 500, 100],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
	});
	
	if(updateAll == 1 || (control.amplitude != preControl.amplitude), 
	{
		wrap.set(\amp, Env.new([0.0, 0.25, 1.0, 3.0, 8.0],[0.25, 0.25, 0.25, 0.25]).at(control.amplitude));
	});
	
	if(updateAll == 1 || (control.entropy != preControl.entropy), 
	{
		wrap.set(\entropy, Env.new([0.001, 0.2, 1.0, 8, 16],[0.25, 0.25, 0.25, 0.25]).at(control.entropy));
	});
	
	if(updateAll == 1 || (control.color != preControl.color), 
	{
		reverb.set(\amp, Env.new([0.99, 1.0, 1.2, 1.4, 1.8],[0.25, 0.25, 0.25, 0.25]).at(control.color));
		reverb.set(\mix, Env.new([0.0, 0.1, 0.35, 0.6, 0.9],[0.25, 0.25, 0.25, 0.25]).at(control.color));
	});
	
	preControl.copy(this.control);
	
	}
	
	freeEffect {
		if(reverb.synth.isPlaying, {reverb.stop});
		if(effectBus.index.isNil.not, {effectBus.free}); 
	} 
	
	setDescription {
		description = "BGenSpinel";
	}
}