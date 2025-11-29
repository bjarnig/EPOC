
BLPat6 : BGen
{
	var paramValues, reverb, effectBus, <>isPlaying, preControl, <>frequencies, <>waveform, updatedFrequencies;
	*new { |id=0, description, duration=10, control, outBus=0, values, frequencies, waveform=1, load=1|
		^super.newCopyArgs(id, description, duration, control, outBus, nil, values, frequencies, waveform).init(load);
	}

	init {|load=1|

		this.setDescription;
		this.isPlaying = 0;
		preControl = BControl.new;
		if(this.control.isNil, {this.control = BControl.new});

		if(load > 0, {

		this.initEffect.value;
		updatedFrequencies = Array.new(4);
		updatedFrequencies.add(67);
		updatedFrequencies.add(87);
		updatedFrequencies.add(97);
		updatedFrequencies.add(107);
		if(frequencies.isNil, {frequencies = [12000, 11000, 10000, 90000]});
		});
	}

	setParam {|paramName, paramValue|
	if(paramName == \duration, {duration = paramValue});
	if(paramName == \frequencies, {frequencies = paramValue});
	if(paramName == \waveform, {waveform = paramValue});
	if(paramName == \outBus, {outBus = paramValue});
	}

	setParamAndUpdate {|param, value|
	if(this.isPlaying > 0,
	{control.setParamValue(param, value);
	this.update.value;})}

	initEffect {
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\bLPat6Verb, [\in, effectBus, \out, outBus]);
		reverb.play;
	}

	*loadSynthDefs {

		SynthDef(\bLPat6Sine,
		{| out=0, amp=0.5, atk=0.5, sus=10, rel=0.1, surface=0.0, speed=0.001, entropy=0.0, randomSpeed=2, repAtk=0.02, repSus=0.02, repRel=2,
		frequencies=#[80, 100, 120, 140], amplitudes=#[0.1, 1.0, 0.4, 0.8]|
	 	var signal, osca, oscb, oscc, oscd, distorted;
	 	var downsamp, samplerate=8000, bitsize=15;
	 	var env = EnvGen.kr(Env.new([0,1,1,0], [atk,sus,rel], -2), doneAction:2);
		osca = SinOsc.ar(frequencies[0], mul:amplitudes[0]) * EnvGen.ar(Env.new([0, 1, 0.2, 0],[repAtk, repSus, repRel]), Impulse.ar((speed * LFNoise1.ar(freq:randomSpeed, add:1,  mul:entropy))));
		oscb = SinOsc.ar(frequencies[1], mul:amplitudes[1]) * EnvGen.ar(Env.new([0, 1, 0.2, 0],[repAtk, repSus, repRel]), Impulse.ar((speed * LFNoise1.ar(freq:randomSpeed, add:1,  mul:entropy))));
		oscc = SinOsc.ar(frequencies[2], mul:amplitudes[2]) * EnvGen.ar(Env.new([0, 1, 0.2, 0],[repAtk, repSus, repRel]), Impulse.ar((speed * LFNoise1.ar(freq:randomSpeed, add:1,  mul:entropy))));
		oscd = SinOsc.ar(frequencies[3], mul:amplitudes[3]) * EnvGen.ar(Env.new([0, 1, 0.2, 0],[repAtk, repSus, repRel]), Impulse.ar((speed * LFNoise1.ar(freq:randomSpeed, add:1,  mul:entropy))));
		signal = (osca + oscb + oscc + oscd) * amp;
		downsamp = Latch.ar(signal, Impulse.ar(samplerate*0.5));
		distorted = downsamp.round(0.5 ** bitsize);
		signal = (signal * (1 - surface)) + (distorted * surface);
		signal = signal * 0.2;
		signal = signal * amp;
		Out.ar(out, signal);
		}).add;

		SynthDef(\bLPat6Impulse,
		{| out=0, amp=0.5, atk=0.5, sus=10, rel=0.1, surface=0.0, speed=0.001, entropy=0.0, randomSpeed=2, repAtk=0.02, repSus=0.02, repRel=2, envSpeed=1,
		frequencies=#[80, 100, 120, 140], amplitudes=#[0.1, 1.0, 0.4, 0.8]|
	 	var signal, osca, oscb, oscc, oscd, distorted, amps, durs, envAmpMod;
	 	var downsamp, samplerate=8000, bitsize=15;
	 	var env = EnvGen.kr(Env.new([0,1,1,0], [atk,sus,rel], -2), doneAction:2);
		osca = Impulse.ar(frequencies[0], mul:amplitudes[0]) * EnvGen.ar(Env.new([0, 1, 0.2, 0],[repAtk, repSus, repRel]), Impulse.ar((speed * LFNoise1.ar(freq:randomSpeed, add:1,  mul:entropy))));
		oscb = Impulse.ar(frequencies[1], mul:amplitudes[1]) * EnvGen.ar(Env.new([0, 1, 0.2, 0],[repAtk, repSus, repRel]), Impulse.ar((speed * LFNoise1.ar(freq:randomSpeed, add:1,  mul:entropy))));
		oscc = Impulse.ar(frequencies[2], mul:amplitudes[2]) * EnvGen.ar(Env.new([0, 1, 0.2, 0],[repAtk, repSus, repRel]), Impulse.ar((speed * LFNoise1.ar(freq:randomSpeed, add:1,  mul:entropy))));
		oscd = Impulse.ar(frequencies[3], mul:amplitudes[3]) * EnvGen.ar(Env.new([0, 1, 0.2, 0],[repAtk, repSus, repRel]), Impulse.ar((speed * LFNoise1.ar(freq:randomSpeed, add:1,  mul:entropy))));
		signal = (osca + oscb + oscc + oscd) * amp;
		downsamp = Latch.ar(signal, Impulse.ar(samplerate*0.5));
		distorted = downsamp.round(0.5 ** bitsize);
		signal = (signal * (1 - surface)) + (distorted * surface);
		amps = [1,1,0,1,1,1,0,1,1,1,0,0,1,1,0,0];
		durs = [1.8, 0.093, 1.23, 0.175, 0.875, 0.09375, 1.3375, 0.18125, 2.2375, 0.175, 2.1375, 0.1875, 1.9375, 0.19375, 1.0375];
		envAmpMod = EnvGen.kr(Env.new(amps, durs).circle, timeScale:envSpeed);
		signal = signal * envAmpMod;
		signal = signal * 0.2;
		signal = signal * amp;
		Out.ar(out, signal);
		}).add;

		SynthDef(\bLPat6LFTri,
		{| out=0, amp=0.5, atk=0.5, sus=10, rel=0.1, surface=0.0, speed=0.001, entropy=0.0, randomSpeed=2, repAtk=0.02, repSus=0.02, repRel=2,
		frequencies=#[80, 100, 120, 140], amplitudes=#[0.1, 1.0, 0.4, 0.8]|
	 	var signal, osca, oscb, oscc, oscd, distorted;
	 	var downsamp, samplerate=8000, bitsize=15;
	 	var env = EnvGen.kr(Env.new([0,1,1,0], [atk,sus,rel], -2), doneAction:2);
		osca = LFTri.ar(frequencies[0], mul:amplitudes[0]) * EnvGen.ar(Env.new([0, 1, 0.2, 0],[repAtk, repSus, repRel]), LFTri.ar((speed * LFNoise1.ar(freq:randomSpeed, add:1,  mul:entropy))));
		oscb = LFTri.ar(frequencies[1], mul:amplitudes[1]) * EnvGen.ar(Env.new([0, 1, 0.2, 0],[repAtk, repSus, repRel]), LFTri.ar((speed * LFNoise1.ar(freq:randomSpeed, add:1,  mul:entropy))));
		oscc = LFTri.ar(frequencies[2], mul:amplitudes[2]) * EnvGen.ar(Env.new([0, 1, 0.2, 0],[repAtk, repSus, repRel]), LFTri.ar((speed * LFNoise1.ar(freq:randomSpeed, add:1,  mul:entropy))));
		oscd = LFTri.ar(frequencies[3], mul:amplitudes[3]) * EnvGen.ar(Env.new([0, 1, 0.2, 0],[repAtk, repSus, repRel]), LFTri.ar((speed * LFNoise1.ar(freq:randomSpeed, add:1,  mul:entropy))));
		signal = (osca + oscb + oscc + oscd) * amp;
		downsamp = Latch.ar(signal, LFTri.ar(samplerate*0.5));
		distorted = downsamp.round(0.5 ** bitsize);
		signal = (signal * (1 - surface)) + (distorted * surface);
		signal = HPF.ar(signal, 40) + LPF.ar(signal, 18500);
		signal = signal * 0.1;
		signal = signal * amp;
		Out.ar(out, signal);
		}).add;

		SynthDef(\bLPat6Verb,{| Êd1 = 0.1, d2 = 0.15, d3 = 0.2, d4 = 0.25, d5 = 0.4,
Ê Ê Ê	t1 = 1, t2 = 2, t3 = 3, t4 = 4, t5 = 5, f1 = 450, f2 = 850, f3 = 1250, f4 = 2450,
		f5 = 20000, in = 3, out = 0, amp=0.8, delayMult=1.0, decayMult=1.0, filtMult=1.0, mix=0.5, pan=0.0
		frequencies=#[800, 1071, 1353, 1723]|
Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê
Ê Ê Ê	var inB, outB, c0, c1, c2, c3, c4, c5, reso;
	 	inB = Pan2.ar(In.ar(in, 1), pan);
	 	reso = DynKlank.ar(`[frequencies, [0.25, 0.25, 0.25, 0.25], [0.15, 0.08, 0.07, 0.06]], inB); //  * 0.5;
	  	c0 = ((inB * 0.4) + (reso * 0.5)); //  * 0.75;
	  	c1 = LPF.ar(CombC.ar(c0, 1, d1 * delayMult, t1 * decayMult), f1 * filtMult) * 1.2;
Ê Ê Ê Ê	c2 = LPF.ar(CombC.ar(c0, 1, d2 * delayMult, t2 * decayMult), f2 * filtMult) * 1.2;
Ê Ê Ê Ê	c3 = LPF.ar(CombC.ar(c0, 1, d3 * delayMult, t3 * decayMult), f3 * filtMult) * 1.2;
Ê Ê Ê Ê	c4 = LPF.ar(CombC.ar(c0, 1, d4 * delayMult, t4 * decayMult), f4 * filtMult) * 1.2;
Ê Ê Ê Ê	c5 = LPF.ar(CombC.ar(c0, 1, d5 * delayMult, t5 * decayMult), f5 * filtMult) * 1.2;
Ê Ê Ê Ê
Ê Ê Ê Ê	outB = (((c1 + c2 + c3 + c4 + c5) * 0.2) * mix) + (inB * (1 - mix));
		Out.ar(out, outB);

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

		('wf ' ++ this.waveform).postln;

		if((this.waveform < 2) || (this.waveform > 4), {wrap = Bwrap.new(\bLPat6Sine, paramValues)});
		if(this.waveform == 2, {wrap = Bwrap.new(\bLPat6Impulse, paramValues)});
		if(this.waveform == 3, {wrap = Bwrap.new(\bLPat6LFTri, paramValues)});

		wrap.setValues([\out, effectBus, \atk, atk, \sus, sus, \rel, rel, \amp, control.amplitude]);
		('BLPat6 start'.postln);
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
	var newFrequency1, newFrequency2, newFrequency3, newFrequency4, freqMult;
	var newAttack, newRelease, newSustain, newAmp, newharmAmp, newSpeed, newAmplitudes;

	if((updateAll == 1) || (control.frequency != preControl.frequency) || (control.density != preControl.density),
	{
		freqMult = Env.new([0.1, 0.5, 1, 1.25, 2],[0.25, 0.25, 0.25, 0.25]).at(control.frequency);
		updatedFrequencies[0] = ((frequencies[1] * freqMult) + ((frequencies[1] - frequencies[0]) * control.density)).max(40).min(18000);
		updatedFrequencies[1] = ((frequencies[2] * freqMult) + ((frequencies[1] - frequencies[0]) * control.density)).max(40).min(18000);
		updatedFrequencies[2] = ((frequencies[3] * freqMult) + ((frequencies[2] - frequencies[1]) * control.density)).max(40).min(18000);
		updatedFrequencies[3] = ((frequencies[3] * freqMult) + ((frequencies[2] - frequencies[1]) * control.density)).max(40).min(18000);
		wrap.set(\frequencies, updatedFrequencies);
		reverb.set(\frequencies, updatedFrequencies);
	});

	if(updateAll == 1 || (control.entropy != preControl.entropy),
	{
		wrap.set(\entropy, Env.new([0.0, 0.2, 0.5, 1.75, 4],[0.25, 0.25, 0.25, 0.25]).at(control.entropy));
	});

	if(updateAll == 1 || (control.density != preControl.density),
	{
		wrap.set(\repAtk, Env.new([0.0, 0.001, 0.002, 0.008, 0.01]*0.1,[0.25, 0.25, 0.25, 0.25]).at(control.density));
		wrap.set(\repSus, Env.new([0.001, 0.025, 0.04, 0,09, 0.15]*0.1,[0.25, 0.25, 0.25, 0.25]).at(control.density));
		wrap.set(\repRel, Env.new([0.001, 0.08, 0.098, 0.11, 0.15]*0.1,[0.25, 0.25, 0.25, 0.25]).at(control.density));
	});

	if(updateAll == 1 || (control.attack != preControl.attack),
	{
		newAttack = control.attack * duration;
		wrap.set(\atk, newAttack);
	});

	if(updateAll == 1 || (control.position != preControl.position),
	{
		newAmplitudes = Array.new(4);
		newAmplitudes.add(Env.new([0.9, 0.6, 0.3, 0.0],[0.33, 0.33, 0.33]).at(control.position));
		newAmplitudes.add(Env.new([0.6, 0.9, 0.0, 0.3],[0.33, 0.33, 0.33]).at(control.position));
		newAmplitudes.add(Env.new([0.3, 0.0, 0.9, 0.6],[0.33, 0.33, 0.33]).at(control.position));
		newAmplitudes.add(Env.new([0.0, 0.3, 0.6, 0.9],[0.33, 0.33, 0.33]).at(control.position));
		wrap.set(\amplitudes, newAmplitudes);
	});

	if(updateAll == 1 || (control.release != preControl.release),
	{
		newRelease = control.release * duration * 0.9;
		wrap.set(\rel, newRelease);
	});

	if(updateAll == 1 || (control.attack != preControl.attack) || (control.release != preControl.release),
	{
		newSustain = duration - (newAttack + newRelease);
		wrap.set(\sus, newSustain);
	});

	if(updateAll == 1 || (control.amplitude != preControl.amplitude),
	{
		newAmp = Env.new([0.0, 0.2, 0.8, 1.5, 4],[0.25, 0.25, 0.25, 0.25]).at(control.amplitude);
		wrap.set(\amp, newAmp);
	});

	if(updateAll == 1 || (control.speed != preControl.speed),
	{
		wrap.set(\envSpeed, Env.new([4, 1.5, 1, 0.5, 0.125].reverse, [0.25, 0.25, 0.25, 0.25]).at(control.speed));
		wrap.set(\speed, Env.new([0.01, 1, 8, 18, 35],[0.25, 0.25, 0.25, 0.25]).at(control.speed));
	});

	if(updateAll == 1 || (control.color != preControl.color),
	{
		reverb.set(\mix, Env.new([0.0, 0.01, 0.08, 0.1, 0.2],[0.25, 0.25, 0.25, 0.25]).at(control.color));
	});

	if(updateAll == 1 || (control.surface != preControl.surface),
	{
		wrap.set(\surface, Env.new([0.0, 0.1, 0.2, 0.5, 0.75],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
		wrap.set(\samplerate, Env.new([44100, 30000, 18000, 8000, 1000],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
	});

	if(updateAll == 1 || (control.location != preControl.location),
	{
		reverb.set(\pan, control.location.linlin(0.0, 1.0, -0.8, 0.8));
	});

	preControl.copy(this.control);

	}

	freeEffect {
		if(reverb.synth.isPlaying, {reverb.stop});
		if(effectBus.index.isNil.not, {effectBus.free});
	}

	setDescription {
		description = "BLPat6: simple sine patterns";
	}
}