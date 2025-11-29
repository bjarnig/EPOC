
BGenPyro : BGen
{
	var paramValues, reverb, effectBus, <>isPlaying, preControl;
	*new { |id=0, description, duration=10, control, outBus=0, values, load=1|
		^super.newCopyArgs(id, description, duration, control, outBus, nil, values).init(load);
	}

	init {|load=1|
		this.setDescription;
		this.isPlaying = 0;
		this.control = BControl.new;
		preControl = BControl.new;
		if(load > 0, { this.initEffect.value });
	}

	setParam {|paramName, paramValue|
	if(paramName == \duration, {duration = paramValue});
	if(paramName == \outBus, {outBus = paramValue});
	}

	initEffect {
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\pyroCombi, [\in, effectBus, \out, outBus]);
		reverb.play;
	}

	*loadSynthDefs {

	 	SynthDef(\pyro,
		{| out=0, freq1=80, freq2=90, freq3=100, speed=80, amp=0.5, atk=0.5, sus=1, rel=0.1, surface=0.0, color=0.5, entropy=0.0, entropyFreq=1, filter=1000, density=0.5, pan=0.0|
	 	var env, osca, oscb, oscc, oscd, signal, speedMod;
	 	env = EnvGen.kr(Env.new([0,1,1,0], [atk,sus,rel], 1), doneAction:2);
	 	freq1= freq1 + (10 * LFNoise1.ar(freq:2*entropyFreq, add:1,  mul:entropy));
	 	freq2= freq2 + (10 * LFNoise1.ar(freq:3*entropyFreq, add:1,  mul:entropy));
	 	freq3= freq3 + (10 * LFNoise1.ar(freq:4*entropyFreq, add:1,  mul:entropy));
	 	speedMod = ((speed * 2) * LFNoise1.ar(freq:8*entropyFreq, add:1, mul:entropy));
		osca = DynKlank.ar(`[[freq1, freq1 * 2, freq1 * 3], nil, [0.5*color, 0.2*color, 0.2*color]], Impulse.ar(speed + speedMod));
	 	oscb = DynKlank.ar(`[[freq2, freq2 * 2, freq2 * 3], nil, [0.5*color, 0.2*color, 0.2*color]], Impulse.ar((speed * 2) + speedMod, mul:density));
	 	oscc = DynKlank.ar(`[[freq3, freq3 * 2, freq3 * 3], nil, [0.5*color, 0.2*color, 0.2*color]], Impulse.ar((speed * 8) + speedMod, mul:density));
		signal = osca + oscb + oscc;
		signal = signal * 0.1;
		signal = BPeakEQ.ar(signal, filter, 8, 0.1 + 10 * surface);
		signal = (signal * (1 - surface) + (signal.round(0.1) * (surface)));
		signal = LeakDC.ar(signal);
		signal = HPF.ar(signal, 70);
		signal = signal * env;
		signal = signal * amp;
		signal = Pan2.ar(signal, pan);
		Out.ar(out, signal);
		}, [0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1]
		).add;

		SynthDef(\pyroCombi,
		{| 	d1 = 0.9, d2 = 0.95, d3 = 1.0, d4 = 1.1, d5 = 2,
	 	 		t1 = 2, t2 = 4, t3 = 8, t4 = 16, t5 =32, f1 = 10000, f2 = 12000, f3 = 15000, f4 = 19000,
		f5 = 20000, in = 3, out = 0, amp=0.8, delayMult=0.1, decayMult=1.0, filtMult=1.0, mix=0.5|
	 	 	 	 	 	 	 	 	 	 	 	 	 	 	 	 	 	 	 	
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
		wrap = Bwrap.new(\pyro, paramValues);
		wrap.setValues([\out, effectBus, \atk, atk, \sus, sus, \rel, rel, \amp, control.amplitude]);
		('Pyro start'.postln);
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

	var newFrequency1, newFrequency2, newFrequency3, newFrequency4;
	var newAttack, newRelease, newSustain, newAmp, newColor, newSpeed;

	if(updateAll == 1 || (control.attack != preControl.attack) || (control.release != preControl.release),
	{
		newAttack = control.attack * duration;
		newRelease = control.release * duration;
		newSustain = duration - (newAttack + newRelease);
		wrap.set(\atk, newAttack);
		wrap.set(\sus, newSustain);
		wrap.set(\rel, newRelease);
	});

	if(updateAll == 1 || (control.speed != preControl.speed),
	{
		newSpeed = Env.new([0.0001, 2, 8, 15, 100],[0.25, 0.25, 0.25, 0.25]).at(control.speed);
		wrap.set(\speed, newSpeed);
	});

	if(updateAll == 1 || (control.density != preControl.density),
	{
		wrap.set(\density, Env.new([0.0001, 0.3, 0.5, 0.8, 1.5],[0.25, 0.25, 0.25, 0.25]).at(control.density));
	});

	if(updateAll == 1 || (control.frequency != preControl.frequency),
	{
		newFrequency1 = 100 * control.frequency;
		newFrequency2 = 150 * control.frequency;
		newFrequency3 = 200 * control.frequency;
		newFrequency4 = 300 * control.frequency;
		wrap.set(\freq1, newFrequency1);
		wrap.set(\freq2, newFrequency2);
		wrap.set(\freq3, newFrequency3);
		wrap.set(\filter, 80 + (16000 * control.frequency));
	});

	if(updateAll == 1 || (control.entropy != preControl.entropy),
	{
		wrap.set(\entropy, control.entropy);
		wrap.set(\entropyFreq, Env.new([0.5, 0.8, 1.0, 1.5, 8],[0.25, 0.25, 0.25, 0.25]).at(control.entropy));
	});

	if(updateAll == 1 || (control.amplitude != preControl.amplitude),
	{
		newAmp = control.amplitude.linlin(0.0, 1.0, 0.0, 2.0);
		wrap.set(\amp, newAmp);
	});

	if(updateAll == 1 || (control.color != preControl.color),
	{
		reverb.set(\mix, (control.color * 0.8));
		reverb.set(\decayMult, ((1 - control.color) * 0.5) + 0.05);
	});

	if(updateAll == 1 || (control.surface != preControl.surface),
	{
		wrap.set(\surface, control.surface);
	});

	if(updateAll == 1 || (control.position != preControl.position),
	{
		wrap.set(\color, Env.new([0.001, 0.2, 1.0, 1.5, 4],[0.25, 0.25, 0.25, 0.25]).at(control.position));
	});

	if(updateAll == 1 || (control.location != preControl.location),
	{
		wrap.set(\pan, Env.new([-1, 0.3, 0.0, 0.3, 1],[0.25, 0.25, 0.25, 0.25]).at(control.location));
	});

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
		description = "";
	}
}