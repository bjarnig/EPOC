
BLSynth7 : BGen
{
	var paramValues, reverb, effectBus, <>isPlaying, preControl, <>durations, <>amplitudes, <>curveType;

	*new { |id=0, description, duration=10, control, outBus=0, values, durations, amplitudes, curveType=0, load=1|
		^super.newCopyArgs(id, description, duration, control, outBus, nil, values, durations, amplitudes, curveType).init(load);
	}

	init {|load=1|

		this.setDescription;
		this.isPlaying = 0;
		preControl = BControl.new;
		if(this.control.isNil, {this.control = BControl.new});

		if(load > 0, {

		this.initEffect.value;

		if(durations.isNil, {
			durations = [1.5, 1, 2, 4, 0.5, 1];
			if(this.curveType == 1, {durations = [4, 2, 1, 0.5, 0.25, 0.15]});
			if(this.curveType == 2, {durations = [1.5, 0.5, 2, 4, 2, 1.5]});
			if(this.curveType == 3, {durations = [0.25, 0.75, 0.25, 1, 2, 4]});
			if(this.curveType == 4, {durations = [1.5, 1, 2, 4, 1.5, 1]});
		});

		if(amplitudes.isNil, {
			amplitudes = [0.1, 1.0, 0.2, 1.0, 0.1, 0.9, 0.1];
			if(this.curveType == 1, {amplitudes = [0.1, 1.0, 0.2, 1.0, 0.1, 0.9, 0.1]});
			if(this.curveType == 2, {amplitudes = [0.1, 1.0, 0.13, 1.0, 0.4, 0.9, 0.1]});
			if(this.curveType == 3, {amplitudes = [0.05, 1.0, 0.05, 0.8, 0.01, 1.0, 0.05]});
			if(this.curveType == 4, {amplitudes = [1.0, 0.4, 0.99, 0.0, 1.0, 0.2, 1.0]});
			});
		});
	}

	setParam {|paramName, paramValue|
	if(paramName == \duration, {duration = paramValue});
	if(paramName == \durations, {durations = paramValue});
	if(paramName == \amplitudes, {amplitudes = paramValue});
	if(paramName == \curveType, {curveType = paramValue});
	if(paramName == \outBus, {outBus = paramValue});
	}

	setParamAndUpdate {|param, value|
	if(this.isPlaying > 0,
	{control.setParamValue(param, value);
	this.update.value;})}

	initEffect {
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\bLSynth7Verb, [\inBus, effectBus, \outBus, outBus]);
		reverb.play;
	}

	*loadSynthDefs {

	SynthDef(\bLSynth7, {
	arg outBus=0, amp=0.5, atk=0.5, sus=10, rel=0.1, pan=0.0, frequencyA=100, frequencyB=1000, lop=18000, hip=20, lopVol=0.5, hipVol=0.5,
	envTime=1, envShape=1, entropy=14,
	durations=#[1.5, 1, 2, 4, 0.5, 1], amplitudes=#[0.1, 1.0, 0.2, 1.0, 0.1, 0.9, 0.1];
	var signal;
	signal = LPCError.ar(Resonz.ar(Mix(Saw.ar(frequencyA*[1,0.9,0.8,0.7,0.5])),frequencyB,0.1,2), entropy);
	signal = signal * 0.0001;
	signal = Limiter.ar(signal, 0.99, 0.0001);
	signal = BLowPass4.ar(signal, lop, 0.25, mul:lopVol) + BHiPass4.ar(signal, hip, 0.25, mul:hipVol);
	signal = signal * EnvGen.kr(Env.new([0, 1, 1, 0],[atk, sus, rel]));
	signal = signal * EnvGen.kr(Env.new(amplitudes, durations, [envShape, envShape * (-1)]).circle, timeScale:envTime);
	signal = signal * amp;
	signal = Pan2.ar(signal, pan);
	Out.ar(outBus, signal);
	}).add;

	SynthDef(\bLSynth7Verb, {
	arg dryWet=0.2, inBus=0, outBus=2;
	var reverb, ampFollow, output, signal, effect, verb, cleanSignal;
	signal = In.ar(inBus, 2);
	cleanSignal = BLowPass.ar(BHiPass.ar(signal, 500, 0.1, 14000, 0.1));
	effect = GVerb.ar(BBandStop.ar(cleanSignal, 2000, 6.0), roomsize: 25, revtime: 2, damping: 0.9, spread: 0.25, drylevel: 0.5, earlyreflevel:0.3, taillevel:0.6) * 0.8;
	verb = CombC.ar(signal, 0.08, 0.08, 3) + (effect * 0.001);
	output = (verb * dryWet) + (signal * (1-dryWet));
	Out.ar(outBus, output);
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
		wrap = Bwrap.new(\bLSynth7, paramValues);
		wrap.setValues([\outBus, effectBus, \atk, atk, \sus, sus, \rel, rel, \amp, control.amplitude]);
		('BLSynth7 start'.postln);
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
	var newAttack, newSustain, newRelease;

	if(updateAll == 1 || (control.speed != preControl.speed),
	{
		wrap.set(\envTime, Env.new([10, 2, 1, 0.5, 0.05],[0.25, 0.25, 0.25, 0.25]).at(control.speed));
	});

	if(updateAll == 1 || (control.density != preControl.density),
	{
		wrap.set(\envShape, Env.new([20, 10, 2, 0.5, 0.1],[0.25, 0.25, 0.25, 0.25]).at(control.density));
	});

	if(updateAll == 1 || (control.entropy != preControl.entropy),
	{
		wrap.set(\entropy, Env.new([3, 6, 15],[0.5, 0.5]).at(control.entropy));
	});

	if(updateAll == 1 || (control.position != preControl.position),
	{
		wrap.set(\durations, BUtils.limitArrayByPositionSameSize(control.position, durations));
		wrap.set(\amplitudes, BUtils.limitArrayByPositionSameSize(control.position, amplitudes));
	});

	if((updateAll == 1) || (control.frequency != preControl.frequency) || (control.density != preControl.density),
	{
		wrap.set(\frequencyA, Env.new([100, 4000, 10000],[0.5, 0.5]).at(control.frequency));
		wrap.set(\frequencyB, Env.new([100, 500, 1000],[0.5, 0.5]).at(control.frequency));
	});

	if(updateAll == 1 || (control.amplitude != preControl.amplitude),
	{
		wrap.set(\amp, Env.new([0.0, 0.05, 0.5, 1.5, 4.0],[0.25, 0.25, 0.25, 0.25]).at(control.amplitude));
	});

	if(updateAll == 1 || (control.color != preControl.color),
	{
		reverb.set(\dryWet, control.color);
	});

	if(updateAll == 1 || (control.surface != preControl.surface),
	{
		wrap.set(\lop, Env.new([100, 400, 1000, 800, 100],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
		wrap.set(\hip, Env.new([8000, 2000, 1000, 2000, 8000],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
		wrap.set(\lopVol, Env.new([0.95, 0.7, 0.5, 0.25, 0.1],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
		wrap.set(\hipVol, Env.new([0.15, 0.3, 0.5, 0.75, 0.9],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
	});

	if(updateAll == 1 || (control.location != preControl.location),
	{
		wrap.set(\pan, control.location.linlin(0.0, 1.0, -0.8, 0.8));
	});

	if(updateAll == 1 || (control.attack != preControl.attack),
	{
		newAttack = control.attack * duration;
		wrap.set(\atk, newAttack);
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

	preControl.copy(this.control);

	}

	freeEffect {
		if(reverb.synth.isPlaying, {reverb.stop});
		if(effectBus.index.isNil.not, {effectBus.free});
	}

	setDescription {
		description = "BLSynth7";
	}
}