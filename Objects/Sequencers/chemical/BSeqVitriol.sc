BSeqVitriol : BSeq
{
	var <>durations, <>sound, currentPattern, reverb, effectBus,
	proxyGrDur, proxyAtk, proxyRel,
	proxySurface, proxyStart, proxyRate, proxyGrAmp, proxyPan, proxySpeed, proxyRepeat, proxyDensity, proxyDur,
	<>isPlaying, wavesets, preControl;

	*new { |id=0, description="BSeqVitriol", duration=10, control, outBus=0, durations, sound, load=1|

		^super.newCopyArgs(id, description, duration, control, outBus, durations, sound).init(load);
	}

	init {|load=1|

		this.setDescription;
		this.isPlaying = 0;
		if(this.control.isNil, {this.control = BControl.new});
		preControl = BControl.new;

		if(load > 0, {
		if(durations.isNil, {this.durations = [0.5, 0.5]});
		if(sound.isNil, {this.sound = BConstants.monoSnd});
		wavesets = Wavesets.from(sound);
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\vitriolVerb, [\inBus, effectBus, \outBus, outBus]);
		reverb.play; });
	}

	setParam {|paramName, paramValue|
	if(paramName == \duration, {duration = paramValue});
	if(paramName == \sound, {sound = paramValue});
	if(paramName == \durations, {durations = paramValue});
	if(paramName == \outBus, {outBus = paramValue});
	}

	setParamAndUpdate {|param, value|
	if(this.isPlaying > 0,
	{control.setParamValue(param, value);
	this.update.value;})}

	*loadSynthDefs {

		SynthDef(\vitriol, { arg out=0, buf=0, start=0, length=441, playRate=1, sustain=1, amp=0.2, grAmp=1.0, pan=0, lengthMult=1.0, surface=1000;
		var phasor = Phasor.ar(0, BufRateScale.ir(buf) * playRate, 0, length * lengthMult) + start;
		var env = EnvGen.ar(Env([amp, amp, 0], [sustain, 0]), doneAction: 2);
		var snd = BPeakEQ.ar(BufRd.ar(1, buf, phasor) * env * amp, surface, 4, 3);
		OffsetOut.ar(out, Pan2.ar(snd, pan, grAmp));
		}, \ir.dup(8)).add;


		SynthDef(\vitriolVerb, {| outBus = 0, inBus=2, amp=1, dryWet=0.01, roomsize=240, revtime=4.85, damping=0.21, inputbw=0.19,
		earlylevel=(-12), taillevel=(-11), spread = 15|
		var freqs, ringtimes, input, signal;
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

	update {

		var pat, atk, rel, sus;
		var ws1Speed, ws1ReadFrom, ws1ReadTo, ws1Start, ws1Rate, ws1GrAmp, ws1Pan, ws1Surface, ws1Repeat, ws1Density, ws1Dur;

		if(control.speed > 0.97, {control.speed = 0.97});
		ws1ReadFrom = (wavesets.numXings-50) * control.position;
		ws1ReadTo = wavesets.numXings-ws1ReadFrom * (control.density + 0.01);
		ws1Start = Pn(Pseries(ws1ReadFrom, 1, ws1ReadTo), inf) + Pfunc({ (50 * control.entropy).rand2 });
		ws1Speed = Env.new([16, 8, 1, 1, 1], [0.25, 0.25, 0.25, 0.25]).at(control.speed);
		ws1Repeat = [1, 1, 2, 3, 4].at((4 * control.surface).ceil);
		ws1Surface = Env.new([80, 400, 1000, 4000, 8000], [0.25, 0.25, 0.25, 0.25]).at(control.surface);
		ws1Rate = Env.new([0.1, 0.5, 1, 2, 4], [0.25, 0.25, 0.25, 0.25]).at(control.frequency);
		ws1GrAmp = Env.new([0.00, 0.15, 0.5, 0.75, 1.0], [0.25, 0.25, 0.25, 0.25]).at(control.amplitude);
		ws1Pan = Env.new([-0.9, -0.5, 0, 0.5, 0.9], [0.25, 0.25, 0.25, 0.25]).at(control.location);
		ws1Density = [1, 1, 1, 1, 1].at((4 * control.density).ceil);
		ws1Dur = Env.new([1.1, 1.05, 1, 0.5, 0.25], [0.25, 0.25, 0.25, 0.25]).at(control.speed);

		atk = control.attack * duration;
		rel = control.release * duration;
		sus = duration - (atk + rel);

		this.rev;

	   	if(this.isPlaying > 0,
		{
			if(control.speed != preControl.speed, {proxySpeed.source = ws1Speed; proxyDur = ws1Dur});
			if(control.surface != preControl.surface, {proxyRepeat.source = ws1Repeat; proxySurface.source = ws1Surface});
			if(control.frequency != preControl.frequency, {proxyRate.source = ws1Rate});
			if(control.amplitude != preControl.amplitude, {proxyGrAmp.source = ws1GrAmp});
			if(control.location != preControl.location, {proxyPan.source = ws1Pan});
			if(control.density != preControl.density, {proxyDensity.source = ws1Density});
			if(control.position != preControl.position || control.density != preControl.density || control.entropy != preControl.entropy, {proxyStart.source = ws1Start});
		},
		{
			('Vitriol start'.postln);
			proxySpeed = PatternProxy(ws1Speed);
			proxyDur = PatternProxy(ws1Dur);
			proxySurface = PatternProxy(ws1Surface);
			proxyStart = PatternProxy(ws1Start);
			proxyRate = PatternProxy(ws1Rate);
			proxyGrAmp = PatternProxy(ws1GrAmp);
			proxyPan = PatternProxy(ws1Pan);
			proxyRepeat = PatternProxy(ws1Repeat);
			proxyDensity = PatternProxy(ws1Density);

			currentPattern.stop;

			pat = Pbind(
			\instrument, \vitriol,
			\numWs, proxySpeed,
			\startWs, proxyStart,
			\out, effectBus,
			\playRate, proxyRate,
			\buf, wavesets.buffer.bufnum,
			\repeats, proxyRepeat,
			\pan, proxyPan,
			\grAmp, proxyGrAmp,
			\amp, Pif(Ptime(inf) <= (atk+sus+rel), Env.new([0, 1, 1, 0],[atk, sus, rel], 'sine')),
			\evDur, proxyDur,
			\surface, proxySurface,
			\lengthMult, proxyDensity,
			[\start, \length, \sustain], Pfunc({ |ev|
			var start, length, wsDur;

			#start, length, wsDur = wavesets.frameFor(ev[\startWs], ev[\numWs]);
			[start, length, wsDur * ev[\repeats] / ev[\playRate].abs]
		}),
		\dur, Pkey(\sustain) * Pkey(\evDur)
		);

		this.playDuration(pat);
		this.isPlaying = 1;

		});

		preControl.copy(this.control);
	}

	rev {
		if(control.color != preControl.color, {reverb.set(\dryWet, control.color)});
	}

	play {
		this.update.value;
	}

	playDuration
	{|pat|

		Routine
		{
			1.do({
		     currentPattern = pat.play;
			duration.wait;
			currentPattern.stop;
			this.isPlaying = 0;
		})
		}.play;
	}

	stop {arg release=0;

		Routine
		{
			1.do({

			if(release > 0, {
			proxyGrAmp.source = Pif(Ptime(inf) <= (release), Env.new([control.amplitude, 0],[release], [-2]));
			});

			release.wait;
			currentPattern.stop;
			this.isPlaying = 0;
		})
		}.play;
	}

	dispose {

		this.freeEffect;
		wavesets.clear;
		wavesets.buffer.free
	}

	freeEffect {
		 if(reverb.synth.isPlaying, {reverb.stop});
		 if(effectBus.index.isNil.not, {effectBus.free});
	}

	setDescription {
		description = "BSeqVitriol";
	}
}