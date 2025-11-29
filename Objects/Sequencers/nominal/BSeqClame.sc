
BSeqClame : BSeq
{
	var <>durations, <>sounds, currentPattern, reverb, effectBus, proxyDur, proxyGrDur, proxyAtk, proxyRel, proxyOffset, proxyRate, proxyPan, proxyGrAmp, proxyBuf, isPlaying, buf, lastDur;

	*new { |id=0, description, duration, control, outBus=0, durations, sounds|

		^super.newCopyArgs(id, description, duration, control, outBus, durations, sounds, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil).init();
	}

	init {
		lastDur = 0;
		this.setDescription;
		isPlaying = 0;
	}

	*loadSynthDefs {

		SynthDef(\clame,
		{|bus = 0, amp=1, pan=0, grdur=2.1, grAmp, atk=0.01, rel=0.01, rate=1, offset=0, buf=0, lop=20000, hip=20|
		var env = EnvGen.ar(Env.new([0, amp, amp, 0],[atk,grdur-atk-rel, rel],[8,-8, -4, -4]), doneAction:2);
		var signal = PlayBuf.ar(1,buf, rate * BufRateScale.ir(buf), 1, offset * BufFrames.ir(buf), 0);
		signal = HPF.ar(signal, 60);
		signal = signal * env * grAmp;
		signal = LPF.ar(signal, lop) + HPF.ar(signal, hip);
		OffsetOut.ar(bus, Pan2.ar(signal, pan));
		}).add;

		SynthDef(\clameReso, {| outBus = 0, inBus=2, amp=1, dryWet=0.01|
		var freqs, ringtimes, input, signal;
		freqs = Control.names([\freqs]).kr(Array.rand(128, 60.0, 1500));
		ringtimes = Control.names([\ringtimes]).kr(Array.rand(128, 0.5, 4));
		input = In.ar(inBus, 2);
		signal = DynKlank.ar(`[
		freqs,
		Array.rand(128, 0.001, 0.9),
		ringtimes
		], input);
		signal = signal * 0.001 * amp * dryWet;
		signal = signal + (input * (1 - dryWet));
		Out.ar(outBus, Pan2.ar(signal, 0));
		}).add;
	}

	update {

		var pat, dur, grDur, readFrom, readTo, rate, atk, sus, rel, grAtk, grRel, amp, pan, reverb, bus1, freqMin, freqMax;
		var decayMin, decayMax, offsetPattern, ratePattern, panPattern, sndIndexLower, sndIndexUpper;

		grDur = 2 * control.density;
		grAtk = (control.surface / 2) * grDur;
		grRel = grAtk;
		amp = control.amplitude;
		atk = control.attack * duration;
		rel = control.release * duration;
		sus = duration - (atk + rel);
		dur = durations * (0.01 + ((1 - control.speed) * 3));
		rate = control.frequency + 0.5;
		pan = control.location;
		readFrom = control.location;
		readTo = readFrom + control.entropy;
		freqMin = 200 * control.frequency;
		freqMax = freqMin + (2000 * 1.0.rand);
		decayMin = 4.rand;
		decayMax = 6.rand;
		offsetPattern = Pwhite(readFrom, readTo);
		ratePattern = Pwhite(0.99 * rate, rate * 1.01);
		panPattern = Pwhite((pan * -1), pan);

		sndIndexUpper = sounds.size * control.location;
		sndIndexLower = sndIndexUpper * (1 - control.entropy);

		if(readTo > 1.0, {readTo = readFrom - control.entropy});
		if(readTo < 0.0, {readTo = 0.0});

		this.rev;

	   	if(isPlaying > 0,
		{
			proxyGrDur.source = grDur;
			proxyAtk.source = grAtk;
			proxyRel.source = grRel;
			proxyOffset.source = offsetPattern;
			proxyRate.source = ratePattern;
			proxyPan.source = panPattern;
			proxyGrAmp.source = amp;
			proxyBuf.source = Pindex(sounds, Pwhite(sndIndexLower, sndIndexUpper), inf);
			if(lastDur != dur, {proxyDur.source = Pseq(dur, inf) * Pwhite(rrand(1 - control.entropy, 1), 1.0)});
		},
		{
			proxyDur = PatternProxy(Pseq(dur, inf) * Pwhite(rrand(1 - control.entropy, 1), 1.0));
			proxyGrDur = PatternProxy(grDur);
			proxyAtk = PatternProxy(grAtk);
			proxyRel = PatternProxy(grRel);
			proxyOffset = PatternProxy(offsetPattern);
			proxyRate = PatternProxy(ratePattern);
			proxyPan = PatternProxy(panPattern);
			proxyGrAmp = PatternProxy(amp);
			proxyBuf = PatternProxy(Pindex(sounds, Pwhite(sndIndexLower, sndIndexUpper), inf));

			currentPattern.stop;
			pat = Pbind(
			\instrument, 'clame',
			\dur, proxyDur,
			\grdur, proxyGrDur,
			\atk, proxyAtk,
			\rel, proxyRel,
			\buf, proxyBuf,
			\offset, 0,
			\rate, proxyRate,
			\pan, proxyPan,
			\grAmp, proxyGrAmp,
			\amp, Pif(Ptime(inf) <= (atk+sus+rel), Env.new([0, 1, 1, 0],[atk, sus, rel], 'sine'))  ,
			\bus, effectBus;
		);

		lastDur = dur;
		this.playDuration(pat);
		isPlaying = 1;

		});
	}

	rev {
		reverb.set(\dryWet, control.color);
	}

	play {

		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\clameReso, [\inBus, effectBus, \outBus, outBus]);
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
			this.freeEffect;
			isPlaying = 0;
		})
		}.play;
	}

	stop {

		Routine
		{
			1.do({
			currentPattern.stop;
			(2).wait;
			this.freeEffect;
			isPlaying = 0;
		})
		}.play;
	}

	freeEffect {
		 if(reverb.synth.isPlaying, {reverb.stop});
		 if(effectBus.index.isNil.not, {effectBus.free});
	}

	setDescription {
		description = "";
	}
}