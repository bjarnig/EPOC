
BGenRutile : BGen
{ 	
	var paramValues, <>sound, reverb, buf, fftBuf, effectBus, <>isPlaying, preControl;
	
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
			fftBuf = Buffer.alloc(Server.local,4096);
			(0.8).wait;
			('Rutile.fftBuf done loading').postln;
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
		reverb = Bwrap.new(\rutileVerb, [\inBus, effectBus, \outBus, outBus]);
		reverb.play;
	}
	
	*loadSynthDefs {
		
	 	SynthDef(\rutileGrain, 
		{|buf=0, fftBuf=1, outBus, rate=1, pos=0.1, atk=4, sus=5, rel=6, amp=1, entropy=0.0, pan=0.0, surface=0.5, speed=0.3, density=0.5, bpFreq=1000, 		bpFreq2=2000 delLeft=0.02, delRight=0.02, ampLeft=0.5, ampRight=0.5, randFrom=0.8, randTo=1.2, randSpeed=10, freeze=0, gaussSpeed=1, gaussDensity=0.25|
		var signal, env, trig, startPos, endPos, totalFrames, outLeft, outRight, rand, chain;
		env = EnvGen.ar(Env.new([0,1,1,0],[atk,sus,rel]), doneAction:2);
		totalFrames = BufFrames.kr(buf); 
		rand = TRand.kr(randFrom, randTo, Dust.kr(randSpeed));
		startPos = ((totalFrames * pos) * rand).min(totalFrames).max(0);
		endPos = startPos + ((totalFrames - startPos) * density);
		signal = BufRd.ar(2, buf, Phasor.ar(Impulse.ar(speed), rate, startPos,  endPos, 1)); 
		chain = FFT(fftBuf, signal);
		chain = PV_Freeze(chain, freeze > 0.5); 
		signal = BPeakEQ.ar(IFFT(chain).dup, bpFreq, 3, -9); 
		signal = Limiter.ar(signal, 0.98, 0.1);
		signal = signal * LFGauss.ar(gaussSpeed,gaussDensity,0,1);
		signal = signal * LFNoise2.ar(100 * entropy, 0.5, 0.5).min(1.0).max(1 - entropy);
		signal = (signal * env) * amp;
		signal = LeakDC.ar(signal); 
		outLeft = DelayC.ar(signal[0], delLeft, delLeft, ampLeft); 
		outRight = DelayC.ar(signal[1], delRight, delRight, ampRight);
		Out.ar(outBus, [outLeft, outRight]); 
		}).add; 
		
		SynthDef(\rutileVerb, 
		{arg outBus = 0, inBus=2, amp=1, mix=0.01, maxDelay=8, delay=0.9, feedback=0.65;
		var fx, sig, fxlevel, level;
		fxlevel = mix;
		level = 1.0 - fxlevel;
		sig = InFeedback.ar(inBus,2); 
		fx = GVerb.ar(sig, roomsize: 85, revtime: 4, damping: 0.2, spread: 0.25, drylevel: 0.5, earlyreflevel:0.6, taillevel:0.3);
� �		Out.ar(outBus, (fx * fxlevel) + (sig * level)) 
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
		wrap = Bwrap.new(\rutileGrain, paramValues);
		wrap.setValues([\outBus, effectBus, \buf, buf, \atk, atk, \sus, sus, \rel, rel, \amp, control.amplitude]);
		('Rutile start'.postln);
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
		fftBuf.free;
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
		wrap.set(\freeze, 0);
		wrap.set(\pos, Env.new([0.05, 0.3, 0.6, 0.75, 1.0],[0.25, 0.25, 0.25, 0.25]).at(control.position));
		Routine.new{
		0.1.wait;	
		wrap.set(\freeze, 1); 
		}.play;
	});

	if(updateAll == 1 || (control.location != preControl.location), 
	{
		wrap.set(\delLeft,  Env.new([0.0, 0.02, 0.04, 0.06, 0.08],[0.25, 0.25, 0.25, 0.25]).at(control.location));
		wrap.set(\delRight,  Env.new([0.08, 0.06, 0.04, 0.02, 0.0],[0.25, 0.25, 0.25, 0.25]).at(control.location));
		wrap.set(\ampLeft,  Env.new([0.7, 0.6, 0.5, 0.4, 0.3],[0.25, 0.25, 0.25, 0.25]).at(control.location));
		wrap.set(\ampRight,  Env.new([0.3, 0.4, 0.5, 0.6, 0.7],[0.25, 0.25, 0.25, 0.25]).at(control.location));
	});
	
	if(updateAll == 1 || (control.frequency != preControl.frequency), 
	{
		wrap.set(\freeze, 0);
		wrap.set(\rate, Env.new([0.25, 0.5, 1.0, 2.0, 4.0],[0.25, 0.25, 0.25, 0.25]).at(control.frequency));
		Routine.new{
		0.1.wait;	
		wrap.set(\freeze, 1); 
		}.play;
	});
	
	if(updateAll == 1 || (control.speed != preControl.speed), 
	{	
		wrap.set(\gaussSpeed, Env.new([10, 1, 0.0001],[0.5, 0.5]).at(control.speed));
		wrap.set(\speed,  Env.new([0.001, 0.05, 0.2, 1, 15],[0.25, 0.25, 0.25, 0.25]).at(control.speed));
		reverb.set(\delay,  Env.new([0.8, 0.6, 0.6, 0.2, 0.1],[0.25, 0.25, 0.25, 0.25]).at(control.speed));
	});
	
	if(updateAll == 1 || (control.density != preControl.density), 
	{
		wrap.set(\density,  Env.new([0.0, 0.25, 0.5, 0.75, 1.0],[0.25, 0.25, 0.25, 0.25]).at(control.density));
		wrap.set(\gaussDensity, Env.new([0.008, 0.5, 0.9],[0.5, 0.5]).at(control.density));
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
		wrap.set(\entropy, Env.new([0.0, 0.25, 0.5, 0.75, 0.99],[0.25, 0.25, 0.25, 0.25]).at(control.entropy));
		wrap.set(\randFrom, Env.new([1.0, 0.9, 0.8, 0.7, 0.6],[0.25, 0.25, 0.25, 0.25]).at(control.entropy));
		wrap.set(\randTo, Env.new([1.0, 1.1, 1.2, 1.3, 1.4],[0.25, 0.25, 0.25, 0.25]).at(control.entropy));
		wrap.set(\randSpeed, Env.new([10, 15, 20, 25, 35],[0.25, 0.25, 0.25, 0.25]).at(control.entropy));
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
		description = "BGenRutile";
	}
}