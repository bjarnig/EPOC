
BLPat1 : BGen
{
	var paramValues, <>sounds, buffers, reverb, effectBus, <>isPlaying, <>preControl;

	*new { |id=0, description, duration=10, control, outBus=0, values, sounds, path, load=1|
		^super.newCopyArgs(id, description, duration, control, outBus, nil, values, sounds).init(load);
	}

	init {|load=1|

		preControl = BControl.new;
		this.setDescription;
		this.isPlaying = 0;
		if(this.control.isNil, {this.control = BControl.new});

		if(load > 0, {
			Routine{
			buffers = this.readFiles(this.sounds).value;
			this.initEffect.value;
			Server.local.sync;
			}.play;
		});
	}

	setParam {|paramName, paramValue|
		if(paramName == \duration, {duration = paramValue});
		if(paramName == \sounds, {sounds = paramValue});
		if(paramName == \outBus, {outBus = paramValue});
	}

	setParamAndUpdate {|param, value|
	if(this.isPlaying > 0,
	{control.setParamValue(param, value);
	this.update.value;})}

	initEffect {
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\bLPat1Verb, [\in, effectBus, \out, outBus]);
 		reverb.play;
	}

	readFiles {arg path;
		var files, buffers;
		if(path.isNil, {path = BConstants.monoImpulses});
		files = SoundFile.collect(path);
		buffers = files.collect { |sf| var b;
		b = Buffer.read(Server.local, sf.path);};
		buffers;
 		^buffers;
	}

	*loadSynthDefs {

		SynthDef(\bLPat1, {
		arg maxBuf=2.1, outBus=0, amp=0.1, atk=0.1, sus=0.5, rel=0.1, firstBuf=0, speed=0.5, freq=0.5, density=0.5, entropy=0.5, surface=0.5,
		freqRandFrom=1.0, freqRandTo=1.0, sineFreq=0.5, grDur=1.0, pos=0, pan=0, hiEqAmt=1, hiEqFreq=8000, loEqAmt=1, loEqFreq=200, bpFreq=1000, envSpeed=1;
		var trate, clk, index, index2, indexOsc, indexOsc2, mod, signal, rate, ampEnv, bufPos, formlet, amps, durs, envAmpMod;
		trate = speed * 12;
		mod = LFNoise1.ar(2 * entropy, mul:0.5, add:0.5).min(1.0).max(1 - entropy);
		mod = mod + SinOsc.ar(0.01, add:1, mul:1);
		indexOsc = SinOsc.ar(speed* mod, mul:0.5, add:0.5) * SinOsc.ar((speed* mod) * 0.25, mul:0.5, add:0.5);
		indexOsc2 = SinOsc.ar((speed*0.5) * mod, mul:0.5, add:0.5) * SinOsc.ar(((speed*0.5) * mod) * 0.25, mul:0.5, add:0.5);
		clk = Impulse.kr(trate);
		index = firstBuf + (indexOsc * maxBuf);
		index2 = firstBuf + (indexOsc2 * maxBuf);
		rate = (2 * freq) * TRand.kr(freqRandFrom, freqRandTo, clk);
		bufPos = pos * BufDur.kr(index);
		signal = TGrains.ar(2, clk, index, rate.max(0.01), bufPos, grDur, pan, amp);
		signal = signal + TGrains.ar(2, clk, index, rate.max(0.01), bufPos, grDur*0.75, pan, amp * density);
		signal = (signal * 0.75) + (signal * ((SinOsc.ar(speed*0.5, 0.5, 0.5) * 0.25)));
		signal = (signal * (1 - (density * 0.5)) * 1.25);
		formlet = (signal * surface) + ((1 - surface) * Formlet.ar(signal, 80 + (100 * rate), 0.1, 0.02));
		signal = (signal * 0.5) + (formlet.tanh * 0.5);
		amps = [1,1,0,0,1,1,0,0,1,1,0,0,1,1,0,0];
		durs = [1.8, 0.093, 0.93, 0.175, 1.875, 0.09375, 0.9375, 0.18125, 2.9375, 0.175, 2.9375, 0.1875, 2.9375, 0.19375, 2.9375];
		envAmpMod =  EnvGen.kr(Env.new(amps, durs).circle, timeScale:envSpeed);
		signal = RHPF.ar(signal, 300, 0.2) * 2;
		signal = signal * envAmpMod;
		signal = BPeakEQ.ar(signal, bpFreq, 3, -9);
		signal = BPeakEQ.ar(signal, hiEqFreq, 12, hiEqAmt) + BPeakEQ.ar(signal, loEqFreq, 12, loEqAmt);
 		signal = signal * EnvGen.ar(Env.new([0, 1, 1, 0],[atk, sus, rel], [-4, 1, -1]));
		Out.ar(outBus, signal);
		}).add;

		SynthDef(\bLPat1Verb,{| Êd1 = 0.8, d2 = 0.9, t1 = 6, t2 = 8, f1 = 2800, f2 = 4600,
		in = 3, out = 0, amp=0.8, delayMult=1.0, decayMult=1.0, filtMult=1.0, mix=0.5, sinspd|
Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê
Ê Ê Ê	var inB, c1, c2, comb, verb, signal, highs;
		var roomsize=50, revtime=3.85, damping=0.81, inputbw=0.19, spread = 15, drylevel=(-6), earlylevel=(-9), taillevel=(-11);

		inB = In.ar(in, 2);
	  	c1 = LPF.ar(CombC.ar(inB, 1, d1 * delayMult, t1 * decayMult), f1 * filtMult);
Ê Ê Ê Ê	c2 = LPF.ar(CombC.ar(inB, 1, d2 * delayMult, t2 * decayMult), f2 * filtMult);
Ê Ê Ê Ê	comb = (c1 + c2) * 0.4;
		highs = Amplitude.kr(HPF.ar(inB, 2000), 0.8, 4, 16).min(1.0).max(0.5);
		verb = GVerb.ar(
		inB,
		roomsize,
		revtime,
		damping,
		inputbw,
		spread,
		drylevel.dbamp,
		earlylevel.dbamp,
		taillevel.dbamp,
		roomsize, 1.4);
		// / SinOsc.ar(sinspd)
		signal = (inB * (1 - mix)) + ((((comb.tanh + verb.tanh)) * highs) * mix);
Ê Ê Ê Ê	signal = HPF.ar(signal, 60);
		Out.ar(out, signal * amp);

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
	{ this.update(0).value; });
	}

	playWrap {
	var atk, sus, rel;

		atk = control.attack * duration;
		rel = control.release * duration;
		sus = duration - (atk + rel);
		wrap = Bwrap.new(\bLPat1);
		wrap.set(\maxBuf, buffers.size);
		wrap.set(\outBus, effectBus);
		wrap.set(\atk, atk);
		wrap.set(\sus, sus);
		wrap.set(\rel, rel);
		wrap.set(\amp, control.amplitude);
		wrap.set(\firstBuf, buffers[0].bufnum);
		('BLPat1 start'.postln);
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
		buffers.do({arg item; item.free;});
		buffers = nil;
	}

	update {arg updateAll=0;

	if(updateAll == 1 || (control.attack != preControl.attack),
	{
		wrap.set(\atk, control.attack * duration);
		wrap.set(\sus, duration - ((control.attack * duration) + (control.release * duration)));
	});

	if(updateAll == 1 || (control.release != preControl.release),
	{
		wrap.set(\rel, control.release * duration * 0.9);
		wrap.set(\sus, duration - ((control.attack * duration) + (control.release * duration)));
	});

	if(updateAll == 1 || (control.amplitude != preControl.amplitude),
	{
		wrap.set(\amp, Env.new([0.0, 0.2, 0.75, 0.1, 2.0], [0.25, 0.25, 0.25, 0.25]).at(control.amplitude));
	});

	if((updateAll == 1) || (control.frequency != preControl.frequency),
	{
		wrap.set(\freq, Env.new([0.0, 0.25, 0.5, 1, 4], [0.25, 0.25, 0.25, 0.25]).at(control.frequency));
	});

	if(updateAll == 1 || (control.speed != preControl.speed),
	{
		reverb.set(\sinspd, Env.new([0.001, 0.5, 1, 2, 8], [0.25, 0.25, 0.25, 0.25]).at(control.speed));
		wrap.set(\speed, Env.new([0.001, 0.5, 1, 2, 8], [0.25, 0.25, 0.25, 0.25]).at(control.speed));
		wrap.set(\envSpeed, Env.new([4, 1.5, 1, 0.5, 0.125].reverse, [0.25, 0.25, 0.25, 0.25]).at(control.speed));
		wrap.set(\sineFreq, Env.new([0.001, 0.2, 1, 2, 4], [0.25, 0.25, 0.25, 0.25]).at(control.speed));
	});

	if(updateAll == 1 || (control.density != preControl.density),
	{
		wrap.set(\grDur, Env.new([0.001, 0.02, 0.08, 0.1, 0.15], [0.25, 0.25, 0.25, 0.25]).at(control.density));
		wrap.set(\density, Env.new([0.01, 0.5, 0.8, 1, 2], [0.25, 0.25, 0.25, 0.25]).at(control.density));
	});

	if(updateAll == 1 || (control.entropy != preControl.entropy),
	{
		wrap.set(\entropy, Env.new([0.0, 0.25, 0.5, 0.75, 1], [0.25, 0.25, 0.25, 0.25]).at(control.entropy));
		wrap.set(\freqRandFrom, Env.new([1.0, 1.0, 1.0, 0.9, 0.8], [0.25, 0.25, 0.25, 0.25]).at(control.entropy));
		wrap.set(\freqRandTo, Env.new([1.0, 1.0, 1.0, 1.1, 1.2], [0.25, 0.25, 0.25, 0.25]).at(control.entropy));
	});

	if(updateAll == 1 || (control.color != preControl.color),
	{
		reverb.set(\mix, Env.new([0.0, 0.1, 0.3, 0.5, 0.75], [0.25, 0.25, 0.25, 0.25]).at(control.color));
	});

	if(updateAll == 1 || (control.surface != preControl.surface),
	{
		wrap.set(\bpFreq, Env.new([8000, 4000, 1000, 500, 100],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
		wrap.set(\surface, Env.new([0.0, 0.1, 0.3, 0.5, 0.75], [0.25, 0.25, 0.25, 0.25]).at(control.surface));
		wrap.set(\hiEqAmt, Env.new([-6, -3, 1.0, 3, 6], [0.25, 0.25, 0.25, 0.25]).at(control.surface));
		wrap.set(\loEqAmt, Env.new([2.8, 1.8, 1.0, -3, -6], [0.25, 0.25, 0.25, 0.25]).at(control.surface));
		wrap.set(\hiEqFreq, Env.new([4000, 5000, 6000, 7000, 8000], [0.25, 0.25, 0.25, 0.25]).at(control.surface));
		wrap.set(\loEqFreq, Env.new([95, 130, 160, 190, 220], [0.25, 0.25, 0.25, 0.25]).at(control.surface));
	});

	if(updateAll == 1 || (control.location != preControl.location),
	{
		wrap.set(\pan, Env.new([-1.0, -0.4, 0.0, 0.4, 1.0], [0.25, 0.25, 0.25, 0.25]).at(control.location));
	});

	if(updateAll == 1 || (control.position != preControl.position),
	{
		wrap.set(\pos, Env.new([0.0, 0.1, 0.3, 0.6, 1.0], [0.25, 0.25, 0.25, 0.25]).at(control.position));
	});

	preControl.copy(this.control);

	}

	freeEffect {
		if(reverb.synth.isPlaying, {reverb.stop});
		if(effectBus.index.isNil.not, {effectBus.free});
	}

	setDescription {
		description = "BLPat1: SineReading granulator.";
	}
}