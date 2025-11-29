
BGenLitharge : BGen
{
	var paramValues, reverb, effectBus, <>isPlaying, preControl, <>durations, <>amplitudes, <>noiseType;

	*new { |id=0, description, duration=10, control, outBus=0, values, durations, amplitudes, noiseType=1, load=1|
		^super.newCopyArgs(id, description, duration, control, outBus, nil, values, durations, amplitudes, noiseType).init(load);
	}

	init {|load=1|
		this.setDescription;
		this.isPlaying = 0;
		preControl = BControl.new;
		if(this.control.isNil, {this.control = BControl.new});

		if(load > 0, {

		this.initEffect.value;
		if(durations.isNil, {durations = [0.5, 1, 0.5, 2, 4, 0.5, 1]});
		if(amplitudes.isNil, {amplitudes = [1.0, 0.1, 0.8, 0.2, 0.4, 1.0, 0.1, 1.0]});

		});
	}

	initEffect {
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\lithargeVerb, [\in, effectBus, \out, outBus]);
		reverb.play;
	}

	setParam {|paramName, paramValue|
		if(paramName == \duration, {duration = paramValue});
		if(paramName == \durations, {durations = paramValue});
		if(paramName == \amplitudes, {amplitudes = paramValue});
		if(paramName == \noiseType, {noiseType = paramValue});
		if(paramName == \outBus, {outBus = paramValue});
	}

	*loadSynthDefs {

	 	SynthDef(\litharge,
		{| out=0, amp=0.5, atk=0.5, sus=10, rel=0.1, surface=0.0, speed=0.001, density=0.5, entropy=0.0, lop=10000, hip=10000, pan=0,
		envTime=1, dustSpeed=100, crackleSpeed,
		brownAmt=1.0, whiteAmt=1.0, pinkAmt=1.0, dustAmt=1.0, durations=#[0.5, 1, 0.5, 2, 4, 0.5, 1], amplitudes=#[0.1, 1.0, 0.4, 0.8, 1.0, 0.1, 0.7, 0.1]|
	 	var env = EnvGen.kr(Env.new([0,1,1,0], [atk,sus,rel], -2), doneAction:2);
	 	var signal = (WhiteNoise.ar(whiteAmt) + PinkNoise.ar(brownAmt) + Crackle.ar(crackleSpeed, mul:pinkAmt) + Dust.ar(dustSpeed, mul:dustAmt)) * 0.5;
		var mod = (signal * LFNoise1.ar(32 * speed) * entropy);
		signal = (signal * (1 - entropy)) + mod;
	 	signal = (RLPF.ar(signal, lop, 1.15) * (1-surface)) + (RHPF.ar(signal, hip, 1.15) * surface);
	 	signal = signal * EnvGen.kr(Env.new(amplitudes, durations).circle, timeScale:envTime);
	 	signal = Limiter.ar(signal * 1.8, 0.99, 0.05);
	 	signal = Pan2.ar(signal, pan, env * amp);
		Out.ar(out, signal);
		}, [0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1]
		).add;

		SynthDef(\lithargeVerb,{| in = 3, out = 0, amp=1.0, mix=0.5, decay=1.0, density=0.25, fund=60, randMin=1.0, randMax=1.0|
	 	 	 	 	 	 	 	 	 	 	 	 	 	 	 	 	 	 	 	
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
		wrap = Bwrap.new(\litharge, paramValues);
		wrap.setValues([\out, effectBus, \atk, atk, \sus, sus, \rel, rel, \amp, control.amplitude]);
		('Litharge start'.postln);
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
	var atk, rel, sus;

	if(updateAll == 1 || (control.release != preControl.release) || (control.attack != preControl.attack),
	{
		atk = control.attack * duration;
		rel = control.release * duration;
		sus = duration - (atk + rel);
		wrap.set(\atk, atk);
		wrap.set(\sus, sus);
		wrap.set(\rel, rel);
	});

	if(updateAll == 1 || (control.amplitude != preControl.amplitude),
	{
		wrap.set(\amp, Env.new([0.0, 0.2, 0.75, 2, 8],[0.25, 0.25, 0.25, 0.25]).at(control.amplitude));
	});

	if((updateAll == 1) || (control.frequency != preControl.frequency),
	{
		wrap.set(\lop, Env.new([100, 600, 1000, 400, 1000],[0.25, 0.25, 0.25, 0.25]).at(control.frequency));
		wrap.set(\hip, Env.new([8000, 1000, 2000, 600, 10000],[0.25, 0.25, 0.25, 0.25]).at(control.frequency));
		reverb.set(\fund, Env.new([25, 47, 63, 82, 108],[0.25, 0.25, 0.25, 0.25]).at(control.frequency));
	});

	if(updateAll == 1 || (control.entropy != preControl.entropy),
	{
		wrap.set(\entropy, Env.new([0.0, 0.2, 0.5, 0.75, 0.99],[0.25, 0.25, 0.25, 0.25]).at(control.entropy));
		reverb.set(\randMin, Env.new([1.0, 0.9, 0.8, 0.6, 0.4],[0.25, 0.25, 0.25, 0.25]).at(control.entropy));
		reverb.set(\randMax, Env.new([1.0, 1.1, 1.2, 1.4, 1.6],[0.25, 0.25, 0.25, 0.25]).at(control.entropy));
	});

	if(updateAll == 1 || (control.density != preControl.density),
	{
		wrap.set(\density, Env.new([0.0, 6, 12, 18, 24],[0.25, 0.25, 0.25, 0.25]).at(control.density));
		reverb.set(\density, Env.new([0.01, 0.2, 1, 2, 4],[0.25, 0.25, 0.25, 0.25]).at(control.density));
		reverb.set(\decay, Env.new([0.01, 0.2, 1, 4, 8],[0.25, 0.25, 0.25, 0.25]).at(control.density));
	});

	if(updateAll == 1 || (control.speed != preControl.speed),
	{
		wrap.set(\speed, Env.new([0.0, 0.25, 0.5, 0.75, 1.0],[0.25, 0.25, 0.25, 0.25]).at(control.speed));
		wrap.set(\dustSpeed, Env.new([0.1, 25, 5, 75, 100],[0.25, 0.25, 0.25, 0.25]).at(control.speed));
		wrap.set(\crackleSpeed, Env.new([1.4, 1.55, 1.72, 1.82, 1.99],[0.25, 0.25, 0.25, 0.25]).at(control.speed));
		wrap.set(\envTime, Env.new([4, 1.5, 1, 0.5, 0.05],[0.25, 0.25, 0.25, 0.25]).at(control.speed));
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
		wrap.set(\pan, control.location.linlin(0.0, 1.0, -1.0, 1.0));
	});

	if(updateAll == 1 || (control.position != preControl.position),
	{
		wrap.set(\durations, BUtils.limitArrayByPositionSameSize(control.position, durations));
		wrap.set(\amplitudes, BUtils.limitArrayByPositionSameSize(control.position, amplitudes));

		if(this.noiseType == 2, {
		'crackkkkkle'.postln;
		wrap.set(\crackleAmt, Env.new([0.8, 0.6, 0.4, 0.2],[0.33, 0.33, 0.33]).at(control.position));
		wrap.set(\dustAmt, Env.new([0.2, 0.4, 0.8, 0.8],[0.33, 0.33, 0.33]).at(control.position));
		wrap.set(\brownAmt, 0);
		wrap.set(\whiteAmt, 0);
		},{
		wrap.set(\crackleAmt, 0);
		wrap.set(\dustAmt, 0);
		wrap.set(\brownAmt, Env.new([1.8, 1.6, 1.4, 1.2],[0.33, 0.33, 0.33]).at(control.position));
		wrap.set(\whiteAmt, Env.new([1.2, 1.4, 1.6, 1.8],[0.33, 0.33, 0.33]).at(control.position));
		});
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
		description = "BGenLitharge";
	}
}