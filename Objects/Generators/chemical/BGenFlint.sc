
BGenFlint : BGen
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
		reverb = Bwrap.new(\flintVerb, [\in, effectBus, \out, outBus]);
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

		SynthDef(\flint, {
		arg maxBuf=2.1, outBus=0, amp=0.1, atk=0.1, sus=0.5, rel=0.1, firstBuf=0, speed=0.5, freq=0.5, density=0.5, entropy=0.5,
		freqRandFrom=1.0, freqRandTo=1.0, sineFreq=0.5, grDur=1.0, pos=0, pan=0, surface=0.5, lop=100, hip=4000;
		var trate, clk, index, index2, indexOsc, indexOsc2, mod, signal, rate, ampEnv, bufPos;
		mod = LFNoise1.ar(50 * entropy, mul:0.5, add:2).min(2.0).max(1 - entropy);
		trate = (speed * 20) * mod;
		indexOsc = LFNoise1.ar(speed * 10, mul:0.5, add:0.5);
		clk = Impulse.kr(trate);
		index = firstBuf + (indexOsc * maxBuf);
		rate = (2 * freq) * TRand.kr(freqRandFrom, freqRandTo, clk);
		bufPos = pos * BufDur.kr(index);
		signal = TGrains.ar(2, clk, index, rate.max(0.01), bufPos, grDur, pan, amp);
		signal = signal + TGrains.ar(2, clk, index, rate.max(0.01), bufPos, grDur*0.75, pan, amp * density);
		signal = (RLPF.ar(signal, lop, 1.15) * (1-surface)) + (RHPF.ar(signal, hip, 1.15) * surface);
		signal = (signal * 0.75) + (signal * ((SinOsc.ar(speed*0.5, 0.5, 0.5) * 0.25)));
		signal = (signal * (1 - (density * 0.5)) * 1.75);
		signal = signal * EnvGen.ar(Env.new([0, 1, 1, 0],[atk, sus, rel], [-4, 1, -1]));
		Out.ar(outBus, signal);
		}).add;

		SynthDef(\flintVerb,{| in = 0, out = 0, amp=1.0, mix=0.5, decay=0.75, density=0.25, fund=60, randMin=0.8, randMax=1.2|
	 	 	 	 	 	 	 	 	 	 	 	 	 	 	 	 	 	 	 	
	 	 		var input	, combi1, combi2, output, reso, effect;
		input = In.ar(in, 2);
		reso = input;
		reso = DynKlank.ar(`[[fund,fund*2,fund*3,fund*4,fund*5,fund*6,fund*7,fund*8,fund*9,fund*10],
		[0.05,0.02,0.04,0.06,0.11,0.01,0.15,0.03,0.15,0.02] * rrand(randMin, randMax), [0.5, 0.2, 0.4, 0.6, 0.1, 0.2, 0.15, 0.3, 0.15, 0.2] * decay], input);
		combi1 = LPF.ar(CombC.ar(reso, 1, 1.0 * density, 2 * density), 1000) * 0.16;
	  	 	 		combi2 = LPF.ar(CombC.ar(reso, 1, 0.9 * density, 4 * density), 500) * 0.15;
		effect = (combi1 + combi2 + reso) * 0.5;
		output = ((effect) * mix) + (input * (1 - mix));
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
	{ this.update(0).value; });
	}

	playWrap {
	var atk, sus, rel;

		atk = control.attack * duration;
		rel = control.release * duration;
		sus = duration - (atk + rel);
		wrap = Bwrap.new(\flint);
		wrap.set(\maxBuf, buffers.size);
		wrap.set(\outBus, effectBus);
		wrap.set(\atk, atk);
		wrap.set(\sus, sus);
		wrap.set(\rel, rel);
		wrap.set(\amp, control.amplitude);
		wrap.set(\firstBuf, buffers[0].bufnum);
		('Flint start'.postln);
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
		wrap.set(\amp, Env.new([0.0, 0.25, 0.75, 2, 4], [0.25, 0.25, 0.25, 0.25]).at(control.amplitude));
	});

	if((updateAll == 1) || (control.frequency != preControl.frequency),
	{
		wrap.set(\freq, Env.new([0.0, 0.25, 0.5, 1, 4], [0.25, 0.25, 0.25, 0.25]).at(control.frequency));
		reverb.set(\fund, Env.new([55, 68, 79, 91, 101],[0.25, 0.25, 0.25, 0.25]).at(control.frequency));
		wrap.set(\lop, Env.new([100, 600, 1000, 400, 1000],[0.25, 0.25, 0.25, 0.25]).at(control.frequency));
		wrap.set(\hip, Env.new([8000, 1000, 2000, 600, 10000],[0.25, 0.25, 0.25, 0.25]).at(control.frequency));
	});

	if(updateAll == 1 || (control.speed != preControl.speed),
	{
		wrap.set(\speed, Env.new([0.001, 0.5, 1, 2, 8], [0.25, 0.25, 0.25, 0.25]).at(control.speed));
		wrap.set(\sineFreq, Env.new([0.001, 0.2, 1, 2, 4], [0.25, 0.25, 0.25, 0.25]).at(control.speed));
	});

	if(updateAll == 1 || (control.density != preControl.density),
	{
		wrap.set(\grDur, Env.new([0.001, 0.02, 0.08, 0.1, 0.18], [0.25, 0.25, 0.25, 0.25]).at(control.density));
		wrap.set(\density, Env.new([0.01, 0.5, 1, 1.8, 3], [0.25, 0.25, 0.25, 0.25]).at(control.density));
	});

	if(updateAll == 1 || (control.entropy != preControl.entropy),
	{
		wrap.set(\entropy, Env.new([0.0, 0.25, 0.5, 0.75, 1], [0.25, 0.25, 0.25, 0.25]).at(control.entropy));
		wrap.set(\freqRandFrom, Env.new([1.0, 1.0, 1.0, 0.99, 0.9], [0.25, 0.25, 0.25, 0.25]).at(control.entropy));
		wrap.set(\freqRandTo, Env.new([1.0, 1.0, 1.0, 1.01, 1.1], [0.25, 0.25, 0.25, 0.25]).at(control.entropy));
	});

	if(updateAll == 1 || (control.color != preControl.color),
	{
		reverb.set(\mix, control.color);
	});

	if(updateAll == 1 || (control.surface != preControl.surface),
	{
		wrap.set(\surface, Env.new([0.0, 0.25, 0.5, 0.75, 1.0],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
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
		description = "BGenFlint: Audio rate granulator.";
	}
}