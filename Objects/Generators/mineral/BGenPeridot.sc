
BGenPeridot : BGen
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
		reverb = Bwrap.new(\peridotVerb, [\inBus, effectBus, \outBus, outBus]);
		reverb.play;
	}
	
	*loadSynthDefs {
		
		SynthDef(\peridot, { |outBus=0, startPos=0.0, density=0.5, bufnum, rate=1.0, randSpeed=4, entropy=0.5, speed=0.1, bpFreq=400, atk=0.8, sus=12, rel=0.5,
		amp=1.0, ampLeft=0.5, ampRight=0.5, durations=#[0.2, 0.02, 0.4]|
		var	sig, env, outLeft, outRight; 
		sig = PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum) * rate, loop: 1.0, startPos:(startPos * BufFrames.kr(bufnum)));
		env = EnvGen.kr(Env.new([0,1,1,0], [atk,sus,rel], -2), doneAction:2);
		sig = sig * (EnvGen.ar(Env.new([0, 1, 0.2, 0], durations*speed).circle) + LFNoise2.ar(randSpeed, entropy, 0.0).max(0.0));
		sig = sig + (PlayBuf.ar(2, bufnum, BufRateScale.kr(bufnum) * 1.1, loop: 1.0) * density);
		sig = BPeakEQ.ar(sig, bpFreq, 3, -18);
		sig = (sig * env) * amp;
 		outLeft = sig[0] * ampLeft; 
 		outRight = sig[1] * ampRight; 
		Out.ar(outBus, [outLeft, outRight]); 
		}).add;	
		
		SynthDef(\peridotVerb, {| outBus = 0, inBus=2, amp=1, dryWet=0.01, roomsize=140, revtime=4.85, damping=0.21, inputbw=0.19, 
		earlylevel=(-12), taillevel=(-11), spread = 15|
		var input, signal; 
		input = In.ar(inBus, 2); 
		
		signal = (GVerb.ar( 
		input,
		roomsize, 
		revtime, 
		damping, 
		inputbw, 
		spread, 
		0,
		earlylevel.dbamp, 
		taillevel.dbamp,
		roomsize, dryWet) * 1.3) + (input*(1-dryWet));
		
		Out.ar(outBus, signal);
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
		wrap = Bwrap.new(\peridot, paramValues);
		wrap.setValues([\outBus, effectBus, \bufnum, buf, \atk, atk, \sus, sus, \rel, rel, \amp, control.amplitude]);
		('Peridot start'.postln);
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
		wrap.set(\randSpeed,  Env.new([0.5, 2, 4, 6, 12],[0.25, 0.25, 0.25, 0.25]).at(control.speed));
		wrap.set(\speed,  Env.new([8, 4, 1, 0.2, 0.05],[0.25, 0.25, 0.25, 0.25]).at(control.speed));
		reverb.set(\revtime,  Env.new([4, 3, 2, 1, 0.5],[0.25, 0.25, 0.25, 0.25]).at(control.speed));
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
		wrap.set(\entropy, Env.new([0.001, 0.2, 0.8, 4, 8],[0.25, 0.25, 0.25, 0.25]).at(control.entropy));
	});
	
	if(updateAll == 1 || (control.color != preControl.color), 
	{
		reverb.set(\amp, Env.new([0.99, 1.0, 1.2, 1.4, 1.8],[0.25, 0.25, 0.25, 0.25]).at(control.color));
		reverb.set(\dryWet, Env.new([0.0, 0.1, 0.35, 0.6, 0.9],[0.25, 0.25, 0.25, 0.25]).at(control.color));
	});
	
	preControl.copy(this.control);
	
	}
	
	freeEffect {
		if(reverb.synth.isPlaying, {reverb.stop});
		if(effectBus.index.isNil.not, {effectBus.free}); 
	} 
	
	setDescription {
		description = "BGenPeridot";
	}
}