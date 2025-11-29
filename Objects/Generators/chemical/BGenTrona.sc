
BGenTrona : BGen
{
	var paramValues, reverb, effectBus, <>isPlaying, preControl;
	*new { |id=0, description, duration=10, control, outBus=0, values, load=1|
		^super.newCopyArgs(id, description, duration, control, outBus, nil, values).init(load);
	}

	init {|load=1|
		this.setDescription;
		this.isPlaying = 0;
		preControl = BControl.new;
		if(this.control.isNil, {this.control = BControl.new});
		if(load > 0, { this.initEffect.value });
	}

	setParam {|paramName, paramValue|
	if(paramName == \duration, {duration = paramValue});
	if(paramName == \outBus, {outBus = paramValue});
	}

	initEffect {
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\tronaCombi, [\in, effectBus, \out, outBus]);
		reverb.play;
	}

	*loadSynthDefs {

	 	SynthDef(\trona,
		{| out=0, density = 100, speed=10, freq=2, surface=0.5, entropy=10, amp=0.5, atk=0.01, sus=10, rel=0.01, bpFreq=1000, pan=0.0, dynMult=1 |
		var trigger, dust, signal, env;
		env = EnvGen.ar(Env.new([0, amp, amp, 0],[atk,sus-atk-rel, rel]), doneAction:2);
		dust = Dust.kr(density, 0.5) * speed;
		trigger = dust + (SinOsc.kr(speed*0.5, mul:0.5, add:1) * LFNoise1.ar(density * 0.1, add:1, mul:entropy));
		signal = DynKlank.ar(`[[180, 90, 115, 172, 200]*dynMult, nil, [0.1, 0.2, 0.3, 0.4, 0.1]], Impulse.ar(trigger, mul:1.4-surface));
		signal = signal + (BrownNoise.ar(surface) * EnvGen.ar(Env.new([0, 1, 0],[0.0002, 0.04]), Impulse.ar(trigger)) * 0.8);
		signal = BPeakEQ.ar(signal, bpFreq, 2, 8);
		signal = signal + ((Dust.ar(density * 2) * SinOsc.ar(0.01 * speed)) * (surface + 0.5));
		signal = ((PitchShift.ar(signal, 0.02, freq, 0, 0.0001) * 0.5) + (signal * 0.5));
		signal = Pan2.ar(signal, pan) * 0.5;
		signal = signal * amp * env;
		Out.ar(out, signal);
	 	}, [0.1, 0.1, 0.1, 0.1, 0.1, 0.1]
		).add;

	SynthDef(\tronaCombi,
	{| d1 = 0.4, d2 = 0.5, d3 = 0.55, d4 = 0.45, d5 = 0.53,
		t1 = 1, t2 = 1.2, t3 = 1.3, t4 = 1.4, t5 = 1.5, f1 = 8000, f2 = 4000, f3 = 16000, f4 = 18000,
		f5 = 20000, in = 3, out = 0, amp=0.8, delayMult=0.1, decayMult=16.0, filtMult=1.0, mix=0.5|

		var inB, outB, c1, c2, c3, c4, c5;
		inB = In.ar(in, 2);

		c1 = LPF.ar(CombC.ar(inB, 1, d1 * delayMult, t1 * decayMult), f1 * filtMult);
		c2 = LPF.ar(CombC.ar(inB, 1, d2 * delayMult, t2 * decayMult), f2 * filtMult);
		c3 = LPF.ar(CombC.ar(inB, 1, d3 * delayMult, t3 * decayMult), f3 * filtMult);
		c4 = LPF.ar(CombC.ar(inB, 1, d4 * delayMult, t4 * decayMult), f4 * filtMult);
		c5 = LPF.ar(CombC.ar(inB, 1, d5 * delayMult, t5 * decayMult), f5 * filtMult);

		outB = (((c1 + c2 + c3 + c4 + c5) * 0.4) * mix) + (inB * (1 - mix));
		Out.ar(out, outB * amp);

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
		wrap = Bwrap.new(\trona, paramValues);
		wrap.setValues([\out, effectBus, \atk, atk, \sus, sus, \rel, rel, \amp, control.amplitude]);
		('Trona start'.postln);
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

	if(updateAll == 1, {
	wrap.set(\atk, control.attack);
	wrap.set(\sus, duration);
	wrap.set(\rel, control.release);
	});

	('TRONA Update amp is ' ++ control.amplitude).postln;

	if(updateAll == 1 || (control.density != preControl.density), { wrap.set(\density, control.density.linlin(0.0, 1.0, 0.001, 800))});
  	if(updateAll == 1 || (control.speed != preControl.speed), { wrap.set(\speed, control.speed.linlin(0.0, 1.0, 0.00001, 800))});
	if(updateAll == 1 || (control.frequency != preControl.frequency), { wrap.set(\freq, Env.new([0.0001, 0.2, 0.4, 1.5, 2.5],[0.25, 0.25, 0.25, 0.25]).at(control.frequency));
	wrap.set(\dynMult, Env.new([0.1, 0.5, 1.0, 1.5, 2.5],[0.25, 0.25, 0.25, 0.25]).at(control.frequency))});
	if(updateAll == 1 || (control.surface != preControl.surface), { wrap.set(\surface, control.surface)});
	if(updateAll == 1 || (control.entropy != preControl.entropy), {wrap.set(\entropy, control.entropy.linlin(0.0, 1.0, 0.001, 20))});
	if(updateAll == 1 || (control.amplitude != preControl.amplitude), { wrap.set(\amp, Env.new([0.0, 0.15, 0.3, 0.6, 0.9],[0.25, 0.25, 0.25, 0.25]).at(control.amplitude))});
	if(updateAll == 1 || (control.location != preControl.location), { wrap.set(\pan, Env.new([-1, 0.5, 0, 0.5, 1],[0.25, 0.25, 0.25, 0.25]).at(control.location))});
	if(updateAll == 1 || (control.position != preControl.position), { wrap.set(\delayMult, (control.position));
	wrap.set(\bpFreq, Env.new([60, 200, 800, 4000, 8000],[0.25, 0.25, 0.25, 0.25]).at(control.position))});
	if(updateAll == 1 || (control.color != preControl.color), { reverb.set(\mix, (control.color * 0.8)); reverb.set(\decayMult, ((1 - control.color) * 0.5) + 0.05);
	reverb.set(\amp, Env.new([1.0, 1.1, 1.3, 1.5, 2.0],[0.25, 0.25, 0.25, 0.25]).at(control.position))});
	preControl.copy(this.control);

	}

	setParamAndUpdate {|param, value|
	if(this.isPlaying > 0,
	{control.setParamValue(param, value);
	this.update.value;})}

	freeEffect {
		if(reverb.synth.isPlaying, {reverb.stop});
		if(effectBus.index.isNil.not, {effectBus.free});
	}

	setDescription {
		description = "trona";
	}
}