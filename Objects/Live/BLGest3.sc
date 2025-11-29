BLGest3 : BSeq
{
	var <>durations, <>sound, startPos, currentPattern, reverb, effectBus, proxyDur, proxyGrDur, proxyAtk, proxyRel, proxyRate, proxyRateb, proxyPan, proxyGrAmp, proxySurface, proxyOverlap, <>isPlaying, buf, preControl, pata, patb, proxyBrowna, proxyBrownb;

	*new { |id=0, description="BLGest3", duration=10, control, outBus=0, durations, sound, startPos=0, load=1|

		^super.newCopyArgs(id, description, duration, control, outBus, durations, sound, startPos).init(load);
	}

	init {|load=1|

		this.setDescription;
		this.isPlaying = 0;
		if(this.control.isNil, {this.control = BControl.new});
		preControl = BControl.new;

		if(load > 0, {
		if(durations.isNil, {this.durations = [0.5, 0.5]});
		if(sound.isNil, {this.sound = BConstants.monoSnd});
		buf = Buffer.read(Server.local, sound);
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\bLGest3Reso, [\inBus, effectBus, \outBus, outBus]);
		reverb.play;  });
	}

	setParam {|paramName, paramValue|
	if(paramName == \duration, {duration = paramValue});
	if(paramName == \sound, {sound = paramValue});
	if(paramName == \durations, {durations = paramValue});
	if(paramName == \startPos, {startPos = paramValue});
	if(paramName == \outBus, {outBus = paramValue});
	}

	setParamAndUpdate {|param, value|
	if(this.isPlaying > 0,
	{control.setParamValue(param, value);
	this.update.value;})}

	*loadSynthDefs {

		SynthDef(\bLGest3,
		{|bus = 0, amp=1, pan=0, grdur=2.1, grAmp, atk=0.01, rel=0.8, rate=1, offset=0, buf=0, surface=0.5|
		var distortIn, distort, amount, amCoef, env, signal, distAmt=0.0, nonDistAmt=1.0, eqDb=10;
		env = EnvGen.ar(Env.new([0, amp, amp, 0],[atk,grdur-atk-rel, rel],[8,-8, -4, -4]), doneAction:2);
		signal = PlayBuf.ar(1,buf, rate * BufRateScale.ir(buf), 1, offset * BufFrames.ir(buf), 1);
		signal = HPF.ar(signal, 60);
		signal = signal * env * grAmp;
		signal = signal * 1.4;
		distortIn = HPF.ar(signal, 800);
		distAmt = (surface - 0.5).max(0.0);
		nonDistAmt = (1.0 - distAmt) - 0.25;
		amount = 0.99;
		amCoef= 2*amount/(1-amount);
		distort = MidEQ.ar(LPF.ar((1+amCoef)*distortIn/(1+(amCoef*distortIn.abs)), [3800, 3900])*0.5, 120, 0.7, 8) * 0.85;
		signal = (distort * distAmt) + (signal * nonDistAmt);
		eqDb = 15 * surface;
		signal = MidEQ.ar(signal, 100, 8, eqDb, mul: 0.1) + MidEQ.ar(signal, 1000, 8, eqDb * (-1), mul: 0.2) + MidEQ.ar(signal, 8000, 2, eqDb, mul: 0.1);
		OffsetOut.ar(bus, Pan2.ar(signal[0] + signal[1], pan));
		}).add;

//		SynthDef(\bLGest3Reso, {| outBus = 0, inBus=2, amp=2, dryWet=0.01|
//		var freqs, ringtimes, input, signal;
//		freqs = Control.names([\freqs]).kr(Array.exprand(128, 60.0, 4000));
//		ringtimes = Control.names([\ringtimes]).kr(Array.exprand(128, 0.25, 1.5));
//		input = In.ar(inBus, 2);
//		signal = XiiAdCreVerb.ar(input, 2.25, predelay: 0.01)  * amp * dryWet;
//		signal = LPF.ar(signal, 120) + HPF.ar(signal, 16000);
//		signal = signal + (input * (1 - dryWet));
//		Out.ar(outBus, signal);
//		}).add;

		SynthDef(\bLGest3Reso,
		{arg outBus = 0, inBus=2, amp=1, dryWet=0.01, maxDelay=8, delay=0.9, feedback=0.80;
		var fx, sig, fxlevel, level;
		fxlevel = dryWet;
		level = 1.0 - fxlevel;
		sig = InFeedback.ar(inBus,2);
		fx = sig + LocalIn.ar(2);
		fx = (DelayC.ar(fx, maxDelay, delay) + LPF.ar(DelayC.ar(fx, maxDelay * 1.2, delay * 1.2), 200) + HPF.ar(DelayC.ar(fx, maxDelay * 0.8, delay * 0.8), 4000)) * 0.4;
		LocalOut.ar(fx * feedback);
Ê Ê		Out.ar(outBus, (fx * fxlevel) + (sig * level))
		}).add;

	}

	update {

		var pat, dur, grDur, rate, rateb, atk, sus, rel, grAtk, grRel, amp, reverb, bus1, overlap;
		var decayMin, decayMax, ratePattern, panPattern, seqRep, surface, speedEnvVal, ampEnv;
		var browna, brownb, overlapPat1, overlapPat2, overlapMult;

		grDur =  Env.new([0.05, 0.2, 0.75, 1.5, 4],[0.25, 0.25, 0.25, 0.25]).at(control.density);
		grAtk = control.density;  // (control.surface / 2) * grDur;
		grRel = grAtk;
		amp = Env.new([0.0, 0.2, 0.5, 0.75, 8],[0.25, 0.25, 0.25, 0.25]).at(control.amplitude);
		atk = control.attack * duration;
		rel = control.release * duration;
		sus = duration - (atk + rel);
		speedEnvVal = Env.new([8, 2, 1, 0.75, 0.35],[0.25, 0.25, 0.25, 0.25]).at(control.speed);
		dur = durations * speedEnvVal;
		rate = Env.new([0.25, 0.75, 1, 2, 4],[0.25, 0.25, 0.25, 0.25]).at(control.frequency);
		rateb = Env.new([0.01, 0.25, 0.75, 1.25, 2.2],[0.25, 0.25, 0.25, 0.25]).at(control.frequency);
		panPattern = Pbrown(Env.new([-0.99, -0.5, -0.1, 0.25, 0.75],[0.25, 0.25, 0.25, 0.25]).at(control.location), Env.new([-0.7, -0.25, 0.1, 0.5, 0.99],[0.25, 0.25, 0.25, 0.25]).at(control.location), 0.1, inf);
		surface = control.surface;
		ratePattern = rate;  // Pwhite(0.99 * rate, rate * 1.01);

		if(dur.sum >= duration, {seqRep = 1}, {seqRep = inf});
		this.rev;

		// ('GEST 3 DUR : ' ++ dur).postln;

		// overlap = Pseq(((1 - control.density) + 0.1) * dur, seqRep);
		browna =  Pbrown(control.position, 1.0, Env.new([0.0001, 0.001, 0.1, 0.2, 0.4],[0.25, 0.25, 0.25, 0.25]).at(control.entropy), inf);
		// brownb =  Pbrown(control.position, 1.0, control.entropy * 0.05, inf);

		overlapMult = Env.new([8, 4, 1.0, 0.75, 0.25],[0.25, 0.25, 0.25, 0.25]).at(control.speed);
		overlapPat1 = Pwrand.new([0.22, 0.44, 0.18, 0.66, 0.88, 0.42, 0.32] * overlapMult, [0.1, 0.25, 0.1, 0.25, 0.05, 0.1, 0.15], 2);
		overlapPat2 = Pwrand.new([0.1, 0.14, 0.12, 0.08, 0.16, 0.12, 0.18] * overlapMult, [0.1, 0.25, 0.1, 0.25, 0.05, 0.1, 0.15], 4);
		overlap = Pseq([overlapPat1, overlapPat2], inf);

	   	if(this.isPlaying > 0,
		{
			if(control.density != preControl.density, {proxyGrDur.source = grDur});
			if(control.surface != preControl.surface, {proxyAtk.source = grAtk});
			if(control.surface != preControl.surface, {proxyRel.source = grRel});
			if(control.frequency != preControl.frequency, {proxyRate.source = ratePattern});
			if(control.location != preControl.location, {proxyPan.source = panPattern});
			if(control.amplitude != preControl.amplitude, {proxyGrAmp.source = amp});
			if(control.surface != preControl.surface, {proxySurface.source = surface});
			if(control.speed != preControl.speed || control.density != preControl.density, {proxyOverlap.source = overlap; reverb.set(\delay,  Env.new([0.8, 0.6, 0.6, 0.2, 0.1],[0.25, 0.25, 0.25, 0.25]).at(control.speed))});
			if(control.entropy != preControl.entropy || control.position != preControl.position, {proxyBrowna.source = browna; proxyBrownb.source = brownb;});
		},
		{
			('BLGest3 start'.postln);
			proxyOverlap = PatternProxy(overlap);
			proxyGrDur = PatternProxy(grDur);
			proxyAtk = PatternProxy(grAtk);
			proxyRel = PatternProxy(grRel);
			proxyRate = PatternProxy(ratePattern);
			proxyRateb = PatternProxy(ratePattern);
			proxyPan = PatternProxy(panPattern);
			proxyGrAmp = PatternProxy(amp);
			proxySurface = PatternProxy(surface);
			proxyBrowna = PatternProxy(browna);
			proxyBrownb = PatternProxy(brownb);

			currentPattern.stop;
			ampEnv = Pif(Ptime(inf) <= (atk+sus+rel), Env.new([0, 0.5, 0.5, 0],[atk, sus, rel], 'sine'));

			pat = Pbind(
			\instrument, 'bLGest3',
			\delta, proxyOverlap,
			\grdur, proxyGrDur,
			\atk, proxyAtk,
			\rel, proxyRel,
			\buf, buf,
			\offset, proxyBrowna,
			\rate, proxyRate,
			\pan, proxyPan,
			\grAmp, proxyGrAmp,
			\surface, proxySurface,
			\amp, ampEnv,
			\bus, effectBus);

//			patb = Pbind(
//			\instrument, 'bLGest3',
//			\delta, proxyOverlap,
//			\grdur, proxyGrDur,
//			// \atk, proxyAtk,
//			// \rel, proxyRel,
//			\buf, buf,
//			\offset, proxyBrownb,
//			\rate, proxyRateb,
//			\pan, proxyPan,
//			\grAmp, proxyGrAmp,
//			\surface, proxySurface,
//			\amp, 0,
//			\bus, effectBus);
//
//		pat = Ppar([ pata, patb ]).play;

		this.playDuration(pat);
		this.isPlaying = 1;

		});

		preControl.copy(this.control);
	}

	rev {
		if(control.color != preControl.color, {reverb.set(\dryWet, Env.new([0.0, 0.05, 0.15, 0.2, 0.3],[0.25, 0.25, 0.25, 0.25]).at(control.color))});
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
		buf.free;
	}

	freeEffect {
		 if(reverb.synth.isPlaying, {reverb.stop});
		 if(effectBus.index.isNil.not, {effectBus.free});
	}

	setDescription {
		description = "BLGest3";
	}
}