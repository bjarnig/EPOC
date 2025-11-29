
BSeqMicjaki : BSeq
{
	var <>sound, currentPattern, reverb, effectBus, pat, proxyRate, proxyDur, proxyGrDur, proxyGrAtk, proxyGrRel, proxyOffset, proxyAmp, proxyPan, isPlaying, lastSpeed;

	*new { |id=0, description, duration, control, outBus=0, sound|

		^super.newCopyArgs(id, description, duration, control, outBus, sound, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil).init();
	}

	init {
		this.setDescription;
		isPlaying = 0;
	}

	*loadSynthDefs {

		SynthDef(\micjaki,
		{|bus = 0, amp=1, vol=1, pan=0, grdur=2.1, atk=0.01, rel=0.01, rate=1, offset=0, buf=0|
		var env = EnvGen.ar(Env.new([0, 1, 1, 0],[atk,grdur-atk-rel, rel],[8,-8, -4, -4]), doneAction:2);
		var signal = PlayBuf.ar(1,buf, rate*BufRateScale.ir(buf), 1, offset*BufFrames.ir(buf), 1);
		signal = HPF.ar(signal, 60);
		signal = (signal * (1 - atk) + (signal.round(0.1) * (atk)));
		signal = signal * env * vol * amp;
		OffsetOut.ar(bus, Pan2.ar(signal, pan));
		}).add;

		SynthDef(\micjakiReso, {| Êd1 = 0.08, d2 = 0.09, d3 = 0.1, d4 = 0.15, d5 = 0.2,
Ê Ê Ê	t1 = 1, t2 = 2, t3 = 3, t4 = 4, t5 = 5, f1 = 50, f2 = 150, f3 = 250, f4 = 350,
		f5 = 20000, inBus = 3, outBus = 0, amp=0.8, delayMult=0.4, decayMult=1.5, filtMult=1.0, dryWet=0.5|
Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê
Ê Ê Ê	var inB, outB, c1, c2, c3, c4, c5;
		inB = In.ar(inBus, 1);

	  	c1 = LPF.ar(CombC.ar(inB, 1, d1 * delayMult, t1 * decayMult), f1 * filtMult);
Ê Ê Ê Ê	c2 = LPF.ar(CombC.ar(inB, 1, d2 * delayMult, t2 * decayMult), f2 * filtMult);
Ê Ê Ê Ê	c3 = LPF.ar(CombC.ar(inB, 1, d3 * delayMult, t3 * decayMult), f3 * filtMult);
Ê Ê Ê Ê	c4 = LPF.ar(CombC.ar(inB, 1, d4 * delayMult, t4 * decayMult), f4 * filtMult);
Ê Ê Ê Ê	c5 = LPF.ar(CombC.ar(inB, 1, d5 * delayMult, t5 * decayMult), f5 * filtMult);
Ê Ê Ê Ê
Ê Ê Ê Ê	outB = (((c1 + c2 + c3 + c4 + c5) * 0.4) * dryWet) + (inB * (1 - dryWet));
Ê Ê Ê Ê	Out.ar([outBus, outBus + 1], outB * amp);
		}).add;
	}

	update {

		var grDur, buf, readFrom, readTo, rate, atk, sus, rel, grAtk, grRel, amp, pan, reverb, bus1, freqMin, freqMax, grAmp, speed;
		var ap = 20, apStep = 1/ap, apB = ap * 8, apStepB = 1/apB, apC = ap * 1.5, apStepC = 1/apB, pat1, pat2, pat3, totalPat, durPat1, durPat2, durPat3, durPat;

		buf = Buffer.read(Server.local, sound);
		grDur = 2 * control.density;
		grAtk = (control.surface / 2) * 1.0;
		grRel = grAtk;
		grAmp = control.amplitude;
		amp = control.amplitude.linlin(0.0, 1.0, 0.0, 2.0);
		atk = control.attack * duration;
		rel = control.release * duration;
		sus = duration - (atk + rel);
		speed = control.speed.linlin(0, 1, 0.5, 0.003);
		rate = control.frequency + 0.5;
		pan = control.location;
		readFrom = control.location;
		readTo = readFrom + control.entropy;

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
		this.rev;

		if(isPlaying > 0,
		{
			if(speed != lastSpeed, {proxyDur.source = durPat;});

			proxyRate.source = Pwhite(0.99 * rate, rate*1.01);
			proxyGrDur.source = grDur;
			proxyGrAtk.source = grAtk;
			proxyGrRel.source = grRel;
			proxyOffset.source = Pbrown(readFrom, readTo, 0.01, inf);
			proxyAmp.source = (totalPat * 0.8) * amp;
			proxyPan.source = Pwhite((pan * -1), pan);
		},
		{
			currentPattern.stop;
			proxyRate = PatternProxy(Pwhite(0.99 * rate, rate*1.01));
			proxyDur = PatternProxy(durPat);
			proxyGrDur = PatternProxy(grDur);
			proxyGrAtk = PatternProxy(grAtk);
			proxyGrRel = PatternProxy(grRel);
			proxyOffset = PatternProxy(Pbrown(readFrom, readTo, 0.01, inf));
			proxyAmp = PatternProxy((totalPat * 0.8) * amp);
			proxyPan = PatternProxy(Pwhite((pan * -1), pan));
			isPlaying = 1;
			lastSpeed = speed;

			pat = Pbind(
			\instrument, 'micjaki',
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

		});
	}

	rev {
		reverb.set(\dryWet, control.color * 0.8);
	}

	play {
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\micjakiReso, [\inBus, effectBus, \outBus, outBus]);
		reverb.play;
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
			isPlaying = 0;
			this.stop;
		})
		}.play;
	}

	stop {

		Routine
		{
			1.do({
			currentPattern.stop;
			isPlaying = 0;
			3.wait;
			reverb.stop;
			effectBus.free;
			('free bus: ' ++ effectBus);
		})
		}.play;
	}

	setDescription {
		description = "";
	}
}