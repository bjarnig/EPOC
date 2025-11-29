
BSeqSucrose : BSeq
{
	var <>sound, currentPattern, reverb, effectBus, pat, proxyRate, proxyDur, proxyGrDur, proxyGrAtk, proxyGrRel, proxyOffset, proxyAmp, proxyPan, <>isPlaying, buf, preControl;

	*new { |id=0, description, duration=10, control, outBus=0, sound, load=1|

		^super.newCopyArgs(id, description, duration, control, outBus, sound).init(load);
	}

	init {|load=1|

		this.setDescription;
		this.isPlaying = 0;
		if(this.control.isNil, {this.control = BControl.new});
		preControl = BControl.new;

		if(load > 0, {
		if(sound.isNil, {this.sound = BConstants.monoSnd});
		buf = Buffer.read(Server.local, sound);
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\sucroseReso, [\inBus, effectBus, \outBus, outBus]);
		reverb.play; });
	}

	setParam {|paramName, paramValue|
	if(paramName == \duration, {duration = paramValue});
	if(paramName == \sound, {sound = paramValue;});
	if(paramName == \outBus, {outBus = paramValue});
	}

	setParamAndUpdate {|param, value|
	if(this.isPlaying > 0,
	{control.setParamValue(param, value);
	this.update.value;})}

	*loadSynthDefs {

		SynthDef(\sucrose,
		{|bus = 0, amp=1, vol=1, pan=0, grdur=2.1, atk=0.01, rel=0.01, rate=1, offset=0, buf=0|
		var env = EnvGen.ar(Env.new([0, 1, 1, 0],[atk,grdur-atk-rel, rel],[8,-8, -4, -4]), doneAction:2);
		var signal = PlayBuf.ar(1,buf, rate*BufRateScale.ir(buf), 1, offset*BufFrames.ir(buf), 1);
		signal = HPF.ar(signal, 60);
		signal = (signal * (1 - atk) + (signal.round(0.1) * (atk)));
		signal = signal * env * vol * amp;
		OffsetOut.ar(bus, Pan2.ar(signal, pan));
		}).add;

		SynthDef(\sucroseReso, {| Êd1 = 0.08, d2 = 0.09, d3 = 0.1, d4 = 0.15, d5 = 0.2,
Ê Ê Ê	t1 = 1, t2 = 2, t3 = 3, t4 = 4, t5 = 5, f1 = 50, f2 = 150, f3 = 250, f4 = 350,
		f5 = 20000, inBus = 3, outBus = 0, amp=0.8, delayMult=0.4, decayMult=1.5, filtMult=1.0, dryWet=0.5|
Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê
Ê Ê Ê	var inB, outB, c1, c2, c3, c4, c5;
		inB = In.ar(inBus, 2);

	  	c1 = LPF.ar(CombC.ar(inB, 1, d1 * delayMult, t1 * decayMult), f1 * filtMult);
Ê Ê Ê Ê	c2 = LPF.ar(CombC.ar(inB, 1, d2 * delayMult, t2 * decayMult), f2 * filtMult);
Ê Ê Ê Ê	c3 = LPF.ar(CombC.ar(inB, 1, d3 * delayMult, t3 * decayMult), f3 * filtMult);
Ê Ê Ê Ê	c4 = LPF.ar(CombC.ar(inB, 1, d4 * delayMult, t4 * decayMult), f4 * filtMult);
Ê Ê Ê Ê	c5 = LPF.ar(CombC.ar(inB, 1, d5 * delayMult, t5 * decayMult), f5 * filtMult);
Ê Ê Ê Ê
Ê Ê Ê Ê	outB = (((c1 + c2 + c3 + c4 + c5) * 0.4) * dryWet) + (inB * (1 - dryWet));
Ê Ê Ê Ê	Out.ar(outBus, outB * amp);
		}).add;
	}

	update {

		var grDur, buf, readFrom, readTo, rate, atk, sus, rel, grAtk, grRel, amp, pan, reverb, bus1, freqMin, freqMax, grAmp, speed;
		var ap = 20, apStep = 1/ap, apB = ap * 8, apStepB = 1/apB, apC = ap * 1.5, apStepC = 1/apB, pat1, pat2, pat3, totalPat, durPat1, durPat2, durPat3, durPat;
		var offset, panorama;

		buf = Buffer.read(Server.local, sound);
		grDur = Env.new([0.05, 0.2, 1, 2, 4],[0.25, 0.25, 0.25, 0.25]).at(control.density);
		grAtk = (control.surface / 2) * 1.0;
		grRel = grAtk;
		grAmp = control.amplitude;
		amp = Env.new([0.0, 0.5, 1.5, 2, 6],[0.25, 0.25, 0.25, 0.25]).at(control.amplitude);
		atk = control.attack * duration;
		rel = control.release * duration;
		sus = duration - (atk + rel);
		speed = control.speed.linlin(0, 1, 0.45, 0.002);
		rate = control.frequency + 0.5;

		readFrom = control.position;
		readTo = readFrom + (0.08 + control.entropy);

		if(readTo > 1.0, {readTo = 1 - control.entropy});
		if(readTo < 0.0, {readTo = 0.0});

		pat1 = Pseq([Pseries(0, apStep, ap), Pseries(1, apStep * (-1), ap)], inf);
		pat2 = Pseq([Pseries(0, apStepB, ap), Pseries(1, apStep * (-1), apB)], inf);
		pat3 = Pseq([Pseries(0, apStepC, ap), Pseries(1, apStep * (-1), apC)], inf);
		totalPat = Pseq([pat1, pat2, pat3], inf);
		durPat1 = Pbrown(speed*0.75, speed*1.25, speed * 0.1, 10);
		durPat2 = Pbrown(speed, speed*2, speed * 0.2, 10);
		durPat3 = Pbrown(speed*0.5, speed*2, speed * 0.05, 10);
		durPat = Pseq([durPat1, durPat2, durPat3], inf);

		offset = Pbrown(readFrom, readTo, 0.01, inf);
		pan = control.location.linlin(0.0, 1.0, -0.6, 1.0);
		panorama = Pwhite(Env.new([-1.0, -0.75, 0, 0.25, 0.5],[0.25, 0.25, 0.25, 0.25]).at(control.location),
		Env.new([-0.75, -0.25, 0.1, 0.75, 1.0],[0.25, 0.25, 0.25, 0.25]).at(control.location));

		this.rev;

		if(this.isPlaying > 0,
		{
			if(control.speed != preControl.speed, {proxyDur.source = durPat});
			if(control.frequency != preControl.frequency, {proxyRate.source = Pwhite(0.99 * rate, rate*1.01)});
			if(control.density != preControl.density, {proxyGrDur.source = grDur});
			if(control.surface != preControl.surface, {proxyGrAtk.source = grAtk; proxyGrRel.source = grRel});
			if(control.position != preControl.position || control.entropy != preControl.entropy, {proxyOffset.source = offset});
			if(control.amplitude != preControl.amplitude, {proxyAmp.source = (totalPat * 0.8) * amp});
			if(control.location != preControl.location, {proxyPan.source = panorama});
		},
		{
			('Sucrose start'.postln);
			proxyRate = PatternProxy(Pwhite(0.99 * rate, rate*1.01));
			proxyDur = PatternProxy(durPat);
			proxyGrDur = PatternProxy(grDur);
			proxyGrAtk = PatternProxy(grAtk);
			proxyGrRel = PatternProxy(grRel);
			proxyOffset = PatternProxy(offset);
			proxyAmp = PatternProxy(totalPat * amp);
			proxyPan = PatternProxy(panorama);

			currentPattern.stop;

			pat = Pbind(
			\instrument, 'sucrose',
			\dur, proxyDur,
			\grdur, proxyGrDur,
			\atk, proxyGrAtk,
			\rel, proxyGrRel,
			\buf, buf,
			\offset, proxyOffset,
			\rate, proxyRate,
			\pan, proxyPan,
			\amp, proxyAmp,
			\vol, Pif(Ptime(inf) <= (atk+sus+rel), Env.new([0, 1, 1, 0],[atk, sus, rel], 'sine'))  ,
			\bus, effectBus
		);

		this.playDuration(pat);
		this.isPlaying = 1;

		});

		preControl.copy(this.control);
	}

	rev {
		if(control.color != preControl.color, {reverb.set(\dryWet, control.color * 0.8)});
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
			proxyAmp.source = Pif(Ptime(inf) <= (release), Env.new([control.amplitude, 0],[release], [-2]));
			});

			release.wait;
			currentPattern.stop;
			this.isPlaying = 0;
		})
		}.play;
	}

	dispose {

		this.freeEffect;
		buf.free;
	}

	freeEffect {
		 if(reverb.synth.isPlaying, {reverb.stop});
		 if(effectBus.index.isNil.not, {effectBus.free});
	}

	setDescription {
		description = "BSeqSucrose";
	}
}