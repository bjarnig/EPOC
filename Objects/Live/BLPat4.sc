
BLPat4 : BSeq
{
	var <>durations, <>deltas, <>soundsA, buffersA, currentPattern, reverb, effectBus, distortion, distBus, proxyDur, proxyGrDur, proxyAtk, proxyRel, proxyRate, proxyPan, proxyGrAmp,
	proxyBuf, proxyDist, proxyOffset, proxyDelta, <>isPlaying, preControl;

	*new { |id=0, description="BLPat4", duration=10, control, outBus=0, durations, deltas, soundsA, load=1|

		^super.newCopyArgs(id, description, duration, control, outBus, durations, deltas, soundsA).init(load);
	}

	init {|load=1|

		this.setDescription;
		this.isPlaying = 0;
		if(this.control.isNil, {this.control = BControl.new});
		preControl = BControl.new;

		if(load > 0, {

		if(this.durations.isNil, {this.durations = [0.2, 0.1]});
		if(this.deltas.isNil, {this.deltas = [0.1, 0.2]});
		buffersA = this.readFiles(soundsA).value;

		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\bLPat4Reso, [\inBus, effectBus, \outBus, outBus]);
		reverb.play;

		distBus = Bus.audio(Server.local, 2);
		distortion = Bwrap.new(\bLPat4Crusher, [\inBus, distBus, \outBus, effectBus]);
		distortion.play; });
	}

	setParam {|paramName, paramValue|
	if(paramName == \duration, {duration = paramValue});
	if(paramName == \durations, {durations = paramValue; });
	if(paramName == \deltas, {deltas = paramValue; });
	if(paramName == \soundsA, {soundsA = paramValue; });
	if(paramName == \outBus, {outBus = paramValue});
	}

	setParamAndUpdate {|param, value|
	if(this.isPlaying > 0,
	{control.setParamValue(param, value);
	this.update.value;})}

	readFiles {arg path;
		var files, buffers;
		if(path.isNil, {path = BConstants.monoImpulses});
		files = SoundFile.collect(path);
		buffers = files.collect { |sf| var b;
		b = Buffer.read(Server.local, sf.path);};
		buffers;
		^buffers;
	}

	*loadSynthDefs {

		SynthDef(\bLPat4,
		{|bus = 0, amp=1, pan=0, grdur=2.1, grAmp, atk=0.0, rel=0.0, rate=1, offset=0, buf=0|
		var env = EnvGen.ar(Env.new([0, amp, amp, 0],[atk,grdur-atk-rel, rel],[8,-8, -4, -4]), doneAction:2);
		var signal = PlayBuf.ar(2, buf, rate * BufRateScale.ir(buf), 1, offset * BufFrames.ir(buf), 0);
		signal = signal * env * grAmp;
		signal =  Compander.ar(signal * 2, signal, 1, 1, 0.05, 0.01, 0.01);
		OffsetOut.ar(bus, Balance2.ar(signal[0], signal[1], pan, 1));
		}).add;

		SynthDef(\bLPat4Crusher, {| inBus=0, outBus=0, samplerate=44100, bitsize=16, fxlevel=0.5, level=0.5, pitchShift=1.0, lpf = 19999, hpf=25|
		var fx, sig, bitRedux, distortIn, distort, amount, amCoef, env, distAmt=0.0, nonDistAmt=1.0, eqDb=10;
		sig = InFeedback.ar(inBus, 2);
		bitRedux = (Clipper8.ar(sig) * 0.3) +  (Clipper8.ar(HPF.ar(sig, 4000), -0.4, 0.4) * 0.8);
		bitRedux = LeastChange.ar(bitRedux, PitchShift.ar(sig, 0.01, pitchShift, 0, 0.004));
		bitRedux = bitRedux.tanh;
		//distortIn = sig; // HPF.ar(sig, 800);
//		distAmt = (fxlevel - 0.5).max(0.0);
//		nonDistAmt = (1.0 - distAmt) - 0.25;
//		amount = 0.99;
//		amCoef= 2*amount/(1-amount);
//		distort = MidEQ.ar(LPF.ar((1+amCoef)*distortIn/(1+(amCoef*distortIn.abs)), [3800, 3900])*0.5, 120, 0.7, 8) * 0.85;
//		sig = (((distort + bitRedux) * 0.5) * distAmt) + (sig * nonDistAmt);
//		eqDb = 15 * fxlevel;
		//sig = MidEQ.ar(sig, 100, 8, eqDb, mul: 0.1) + MidEQ.ar(sig, 1000, 8, eqDb * (-1), mul: 0.2) + MidEQ.ar(sig, 8000, 2, eqDb, mul: 0.1);
		sig = BLowPass.ar(BHiPass.ar(sig, hpf, 0.5), lpf, 0.5);
		//Out.ar(outBus, sig * level)
		Out.ar(outBus, (bitRedux*fxlevel) + (sig * level))
		}).add;

		SynthDef(\bLPat4Reso, {| outBus = 0, inBus=2, amp=1, dryWet=0.01, predelay=0.048,combdecay=4, allpassdecay=14|
		var sig, y, z;
		sig = InFeedback.ar(inBus, 2);
		z = DelayN.ar(sig, 0.1, predelay);
		y = Mix.ar(Array.fill(2,{ CombL.ar(z, 0.2, rrand(0.05, 0.2), combdecay).distort }));
		6.do({ y = AllpassN.ar(y, 0.050, rrand(0.03, 0.05), allpassdecay) });
		Out.ar(outBus, ((sig * (1 - dryWet)) + (y * (dryWet*0.5))) * amp);
		}).add;
	}

	update {

		var pat, dur, grDur, readFrom, readTo, rate, atk, sus, rel, grAtk, grRel, amp, pan, reverb, bus1, freqMin, freqMax;
		var decayMin, decayMax, offsetPat, ratePat, panPat, durPat, sndSelPat, offset, deltaPat, newDeltas, newDurs, newBuffers, randFrom, randTo;

		if(control.position < 0.01, {control.position = 0.01});

		grDur = Env.new([0.05, 0.1, 0.3, 0.5, 0.8]*0.5,[0.25, 0.25, 0.25, 0.25]).at(control.density);
		grAtk = Env.new([0.1, 0.2, 0.4, 0.5, 0.8]*0.5,[0.25, 0.25, 0.25, 0.25]).at(control.density);
		grRel = Env.new([0.1, 0.2, 0.4, 0.5, 0.8]*0.5,[0.25, 0.25, 0.25, 0.25]).at(control.density);
		amp = Env.new([0.0, 0.2, 0.8, 1.4, 2],[0.25, 0.25, 0.25, 0.25]).at(control.amplitude);
		atk = control.attack * duration;
		rel = control.release * duration;
		sus = duration - (atk + rel);
		rate = Env.new([0.25, 0.5, 1, 1.5, 4],[0.25, 0.25, 0.25, 0.25]).at(control.frequency);
		ratePat = Pwhite(0.99 * rate, rate * 1.01);
		pan = control.location.linlin(0, 1, -1, 1);
		if(pan > 0.5, {panPat = Pwhite(pan, (pan * 1.4).min(1.0))}, {panPat = Pwhite((pan * 0.6).max(-1.0), pan)});

//		durFrom = Env.new(this.durations,[0.25, 0.25, 0.25, 0.25]).at(control.speed);
//		durTo = Env.new(this.durations * (this.durationFactor + control.entropy),[0.25, 0.25, 0.25, 0.25]).at(control.speed);
//		durs =  Pseq([Pwhite(durFrom, durTo, rrand(52,78)), Pwhite(this.durations[0] * 5, this.durations[1] * 15, rrand(1, 6))], inf);
//		sndSel = Prand(buffersA, inf);

		//readFrom = control.position;
//		readTo = (readFrom + 0.05 + control.entropy).min(1.0);
//		offset = Pwhite(readFrom, readTo, inf);

		randTo = Env.new([1.0, 1.02, 1.08, 1.1, 1.2],[0.25, 0.25, 0.25, 0.25]).at(control.entropy);
		randFrom = Env.new([1.0, 0.9, 0.8, 0.6, 0.5],[0.25, 0.25, 0.25, 0.25]).at(control.entropy);
		newDeltas = BUtils.limitArrayByPositionSameSize(control.position, deltas) * Env.new([4, 2, 1.0, 0.5, 0.125],[0.25, 0.25, 0.25, 0.25]).at(control.speed);
		newDurs = BUtils.limitArrayByPositionSameSize(control.position, durations) * Env.new([0.125, 0.5, 1.0, 2, 4],[0.25, 0.25, 0.25, 0.25]).at(control.density);
		newBuffers = BUtils.limitArrayByPositionSameSize(control.position, buffersA);

		// newDeltas.postln;
		// newDurs.postln;
		// newBuffers.postln;

		// newDurs.do({arg item, index; newDurs[index] = item * rrand(randFrom, randTo)});
		// newDeltas.do({arg item, index; newDeltas[index] = item * rrand(randFrom, randTo)});
		// newBuffers.do({arg item, index; newBuffers[index] = item * rrand(randFrom, randTo)});
		// newDurs.postln;

		offset = 0;
		durPat = Pseq(newDurs, inf) * Pwhite(randFrom, randTo);
		deltaPat = Pseq(newDeltas, inf) * Pwhite(randFrom, randTo);
		sndSelPat = Pseq(newBuffers, inf);
		this.rev;

	   	if(this.isPlaying > 0,
		{
			if(control.density != preControl.density, {proxyGrDur.source = grDur; proxyAtk.source = grAtk; proxyRel.source = grRel; proxyBuf.source = sndSelPat; proxyDur.source = durPat});
			if(control.surface != preControl.surface, {proxyAtk.source = grAtk});
			if(control.surface != preControl.surface, {proxyRel.source = grRel});
			if(control.frequency != preControl.frequency, {proxyRate.source = ratePat});
			if(control.location != preControl.location, {proxyPan.source = panPat});
			if(control.amplitude != preControl.amplitude, {proxyGrAmp.source = amp});
			if(control.speed != preControl.speed, {proxyDur.source = durPat; proxyDelta.source = deltaPat});
			if(control.entropy != preControl.entropy, {proxyBuf.source = sndSelPat; proxyDur.source = durPat});
			if(control.position != preControl.position || control.entropy != preControl.entropy, {proxyBuf.source = sndSelPat; proxyDelta.source = deltaPat; proxyDur.source = durPat });
			if(control.position != preControl.position || control.entropy != preControl.entropy, {proxyOffset.source = offset});
		},
		{
			('BLPat4 start'.postln);
			proxyDelta = PatternProxy(deltaPat);
			proxyDur = PatternProxy(durPat);
			proxyGrDur = PatternProxy(grDur);
			proxyAtk = PatternProxy(grAtk);
			proxyRel = PatternProxy(grRel);
			proxyRate = PatternProxy(ratePat);
			proxyPan = PatternProxy(panPat);
			proxyGrAmp = PatternProxy(amp);
			proxyBuf = PatternProxy(sndSelPat);
			proxyOffset = PatternProxy(offset);

			currentPattern.stop;
			pat = Pbind(
			\instrument, 'bLPat4',
			\dur, proxyDelta,
			\grdur, proxyDur,
			// \atk, proxyAtk,
			// \rel, proxyRel,
			\buf, proxyBuf,
			\offset, 0, // proxyOffset,
			\rate, proxyRate,
			\pan,  proxyPan,
			\grAmp, proxyGrAmp,
			\amp, Pif(Ptime(inf) <= (atk+sus+rel), Env.new([0, 1, 1, 0],[atk, sus, rel], 'sine')),
			\bus, distBus;
		);

		this.playDuration(pat);
		this.isPlaying = 1;

		});

		preControl.copy(this.control);
	}

	rev {

		if(control.color != preControl.color, {reverb.set(\dryWet, Env.new([0.0, 0.002, 0.02, 0.1, 0.2],[0.25, 0.25, 0.25, 0.25]).at(control.color))});
		if(control.surface != preControl.surface, {
		distortion.set(\fxlevel, Env.new([0.0, 0.02, 0.2, 0.65, 0.8],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
		distortion.set(\pitchShift, Env.new([1.0, 0.5, 0.1, 1.5, 2.0],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
		distortion.set(\level, Env.new([1.0, 0.98, 0.6, 0.35, 0.2],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
		distortion.set(\hpf, Env.new([20, 25, 200, 400, 8000],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
		distortion.set(\lpf, Env.new([100, 8000, 19997, 19998, 19999],[0.25, 0.25, 0.25, 0.25]).at(control.surface));

		});
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
		buffersA.do({arg item; item.free;});
	}

	freeEffect {
		 if(reverb.synth.isPlaying, {reverb.stop});
		 if(effectBus.index.isNil.not, {effectBus.free});
		 if(distBus.index.isNil.not, {distBus.free});
		 if(distortion.synth.isPlaying, {distortion.stop});
	}

	setDescription {
		description = "BLPat4";
	}
}