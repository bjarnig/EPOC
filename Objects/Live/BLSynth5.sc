
BLSynth5 : BGen
{
	var paramValues, reverb, effectBus, <>isPlaying, preControl, <>frequencies, <>durations, <>amplitudes, updatedFrequencies;

	*new { |id=0, description, duration=500, control, outBus=0, values, frequencies, durations, amplitudes, load=1|
		^super.newCopyArgs(id, description, duration, control, outBus, nil, values, frequencies, durations, amplitudes).init(load);
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
		if(frequencies.isNil, {frequencies = [67, 89, 113, 139]});
		if(durations.isNil, {durations = [0.5, 1, 0.5, 2, 4, 0.5, 1]});
		if(amplitudes.isNil, {amplitudes = [1.0, 0.1, 0.8, 0.2, 0.4, 1.0, 0.1, 1.0]});
		});
	}

	setParam {|paramName, paramValue|
	if(paramName == \duration, {duration = paramValue});
	if(paramName == \frequencies, {frequencies = paramValue});
	if(paramName == \durations, {durations = paramValue});
	if(paramName == \amplitudes, {amplitudes = paramValue});
	if(paramName == \outBus, {outBus = paramValue});
	}

	setParamAndUpdate {|param, value|
	if(this.isPlaying > 0,
	{control.setParamValue(param, value);
	this.update.value;})}

	initEffect {
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\bLSynth5Verb, [\in, effectBus, \out, outBus]);
		reverb.play;
	}

	*loadSynthDefs {

		SynthDef(\bLSynth5,
		{| out=0, amp=0.5, atk=0.5, sus=10, rel=0.1, surface=0.0, speed=0.001, density=0.5, entropy=0.0, knum=12, lop=10000, hip=10000, newAmp=0.5,
		frequencies=#[80, 100, 120, 140], durations=#[0.5, 1, 0.5, 2, 4, 0.5, 1], amplitudes=#[0.1, 1.0, 0.4, 0.8, 1.0, 0.1, 0.7, 0.0]|
	 	var env = EnvGen.kr(Env.new([0,1,1,0], [atk,sus,rel], -2), doneAction:2);
	 	var randTrigger = Dust.kr(25*speed);
	 	var rand = TRand.kr(1 - entropy, 1 + entropy, randTrigger);
		var signal = Mix.new(Gendy3.ar(freq:frequencies * rand, mul:amp, knum:knum, adparam:surface, ampscale:amp));
		signal = signal + Mix.new(Gendy3.ar(freq:(frequencies * 2) * rand, mul:amp, knum:knum*0.75, adparam:surface*0.5, ampscale:(amp * density)));
	 	signal = (RLPF.ar(signal, lop, 1.15) * (1-surface)) + (RHPF.ar(signal, hip, 1.15) * surface);
	 	signal = Limiter.ar(signal*2, 0.99, 0.05);
		signal = signal * EnvGen.kr(Env.new(amplitudes, durations * (rand) * (1-speed)).circle);
		Out.ar(out, signal * 1.5);
		}, [0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1]
		).add;

	SynthDef(\bLSynth5Verb,{| d1 = 0.1, d2 = 0.15, d3 = 0.2, d4 = 0.25, d5 = 0.4,
		t1 = 1, t2 = 2, t3 = 3, t4 = 4, t5 = 5, f1 = 450, f2 = 850, f3 = 1250, f4 = 2450,
		f5 = 20000, in = 3, out = 0, amp=0.8, delayMult=1.0, decayMult=1.0, filtMult=1.0, mix=0.5, pan=0.0
		frequencies=#[800, 1071, 1353, 1723]|

		var inB, outB, c0, c1, c2, c3, c4, c5, reso;
		inB = Pan2.ar(In.ar(in, 1), pan);
		reso = DynKlank.ar(`[frequencies, [0.25, 0.25, 0.25, 0.25], [0.15, 0.08, 0.07, 0.06]], inB);
		c0 = ((inB * 0.4) + (reso * 0.5));
		c1 = LPF.ar(CombC.ar(c0, 1, d1 * delayMult, t1 * decayMult), f1 * filtMult) * 1.2;
		c2 = LPF.ar(CombC.ar(c0, 1, d2 * delayMult, t2 * decayMult), f2 * filtMult) * 1.2;
		c3 = LPF.ar(CombC.ar(c0, 1, d3 * delayMult, t3 * decayMult), f3 * filtMult) * 1.2;
		c4 = LPF.ar(CombC.ar(c0, 1, d4 * delayMult, t4 * decayMult), f4 * filtMult) * 1.2;
		c5 = LPF.ar(CombC.ar(c0, 1, d5 * delayMult, t5 * decayMult), f5 * filtMult) * 1.2;

		outB = (((c1 + c2 + c3 + c4 + c5) * 0.2) * mix) + (inB * (1 - mix));
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
		wrap = Bwrap.new(\bLSynth5, paramValues);
		wrap.setValues([\out, effectBus, \atk, atk, \sus, sus, \rel, rel, \amp, control.amplitude]);
		('BLSynth5 start'.postln);
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
	var newAttack, newRelease, newSustain, newAmp, newharmAmp, newSpeed;

	if((updateAll == 1) || (control.frequency != preControl.frequency) || (control.density != preControl.density),
	{
		freqMult = Env.new([0.1, 0.5, 1, 8, 32],[0.25, 0.25, 0.25, 0.25]).at(control.frequency);
		updatedFrequencies[0] = ((frequencies[1] * freqMult) + ((frequencies[1] - frequencies[0]) * control.density)).max(30).min(12000);
		updatedFrequencies[1] = ((frequencies[2] * freqMult) + ((frequencies[1] - frequencies[0]) * control.density)).max(30).min(12000);
		updatedFrequencies[2] = ((frequencies[3] * freqMult) + ((frequencies[2] - frequencies[1]) * control.density)).max(30).min(12000);
		updatedFrequencies[3] = ((frequencies[3] * freqMult) + ((frequencies[2] - frequencies[1]) * control.density)).max(30).min(12000);
		wrap.set(\frequencies, updatedFrequencies);
		reverb.set(\frequencies, updatedFrequencies);
	});

	if(updateAll == 1 || (control.entropy != preControl.entropy),
	{
		wrap.set(\entropy, Env.new([0.0, 0.2, 0.5, 0.75, 0.99],[0.25, 0.25, 0.25, 0.25]).at(control.entropy));
	});

	if(updateAll == 1 || (control.density != preControl.density),
	{

		wrap.set(\density, Env.new([0.0, 0.2, 0.4, 0.6, 0.8],[0.25, 0.25, 0.25, 0.25]).at(control.density));
	});

	if(updateAll == 1 || (control.attack != preControl.attack),
	{
		newAttack = control.attack * duration;
		wrap.set(\atk, newAttack);
	});

	if(updateAll == 1 || (control.position != preControl.position),
	{
		//wrap.set(\durations, BUtils.limitArrayByPositionSameSize(control.position, durations));
		//wrap.set(\amplitudes, BUtils.limitArrayByPositionSameSize(control.position, amplitudes));
		wrap.set(\knum, Env.new([0.0, 6, 12, 18, 24],[0.25, 0.25, 0.25, 0.25]).at(control.position));
		wrap.set(\durations, durations);
		wrap.set(\amplitudes, amplitudes);
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
		newAmp = Env.new([0.0, 0.2, 0.8, 1.5, 2],[0.25, 0.25, 0.25, 0.25]).at(control.amplitude);
		wrap.set(\amp, newAmp);
	});

	if(updateAll == 1 || (control.speed != preControl.speed),
	{
		newSpeed = control.speed.linexp(0.0, 1.0, 0.001, 8.0);
		wrap.set(\speed, newSpeed);
	});

	if(updateAll == 1 || (control.color != preControl.color),
	{
		reverb.set(\mix, control.color);
	});

	if(updateAll == 1 || (control.surface != preControl.surface),
	{
		wrap.set(\lop, Env.new([100, 250, 2000, 400, 1000],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
		wrap.set(\hip, Env.new([8000, 2000, 4000, 6000, 10000],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
		wrap.set(\surface, control.surface);
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
		description = "BLSynth5: Gendy through resonators";
	}
}