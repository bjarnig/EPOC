BSeqPotash : BSeq
{
	var <>sound, currentPattern, reverb, effectBus, <>isPlaying, buf, preControl, <>durations, <>amplitudes, <>frequencies,
	proxyGrAmp, proxyPan, proxyRate, proxyDurations, proxyAmplitudes, proxyPos, proxyAtk, proxySus, proxyRel, proxyVibAmt, proxyVibFreq;

	*new { |id=0, description="BSeqPotash", duration=10, control, outBus=0, sound, durations, amplitudes, frequencies|
		^super.newCopyArgs(id, description, duration, control, outBus, sound).init(durations, amplitudes, frequencies);
	}

	init {|durs, amps, freqs|

		this.setDescription;
		this.isPlaying = 0;
		if(this.control.isNil, {this.control = BControl.new});
		if(sound.isNil, {this.sound = "/Users/bjarni/Desktop/SND/test/dfrz.wav"});
		preControl = BControl.new;
		buf = Buffer.read(Server.local, sound);
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\potashEffect, [\inBus, effectBus, \outBus, outBus]);
		reverb.play;
		durations = durs;
 		amplitudes = amps;
 		frequencies = freqs;
		if(durations.isNil, {durations = [8, 6, 5, 7, 9, 4, 6, 0.2, 0.4]});
		if(amplitudes.isNil, {amplitudes = [1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0]});
		if(frequencies.isNil, {frequencies = [1.0, 0.298, 1.22, 0.96, 1.04, 0.5, 2.0]});
	}

	*loadSynthDefs {

		SynthDef(\potash,
		{|amp=1, grAmp=1, damp=1, atk=2, sus=12, rel=8, buf, envbuf, pos=0, rate=1, bus=0, vibSpeed=4, vibAmt=0.3|
		var env, posEnv, signal;
		posEnv = EnvGen.kr(Env.new([pos, 1],[BufDur.kr(buf)]).circle);
		env = EnvGen.kr(Env.new([0, 1, 1, 0], [atk, sus, rel], [2, 1, -2]), levelScale: amp, doneAction: 2);
		signal = (GrainBuf.ar(1, Impulse.kr(2), 2, buf, rate * LinLin.kr(SinOsc.kr(vibSpeed), -1.0, 1.0+vibAmt, 1.0-vibAmt, 1.0), (posEnv).min(1).max(0), 2, 0.0, envbuf, mul:damp) * env) * grAmp;
		OffsetOut.ar(bus, signal);
		}).add;

		SynthDef(\potashEffect, {| outBus=0, inBus=2, amp=1, dryWet=0.01, roomsize=240, revtime=8.85, damping=0.11, inputbw=0.19,
		earlylevel=(-12), taillevel=(-11), spread = 15, pan=0.0, lop=200, hip=4000, surface=0.5, entropy=0.0, eq=4000, speed=0.5|
		var input, signal, mod;
		input = In.ar(inBus, 1);
		signal = BPeakEQ.ar(input, eq, 4, 8);
		mod = (signal * LFNoise1.ar(32 * speed) * entropy);
		signal = (signal * (1 - entropy)) + mod;
		signal = (GVerb.ar(
		signal,
		roomsize,
		revtime,
		damping,
		inputbw,
		spread,
		0,
		earlylevel.dbamp,
		taillevel.dbamp,
		roomsize, dryWet) * 1.3) + (signal*(1-dryWet));

		// signal = input;
		signal = DelayC.ar(Pan2.ar(signal, -1), 0.04, 0.04, mul:pan) + DelayC.ar(Pan2.ar(signal, 1), 0.02, 0.02, mul:(1-pan));
		Out.ar(outBus, signal * amp);
		}).add;
	}

	update {

		var pat, dur, pos, grDur, grAmp, rate, atk, sus, rel, amp, pan, reverb, surface, ratePattern, speed, speedDurs, speedAmps;
		var grAtk, grSus, grRel, vib, vibAmt;

		atk = control.attack * duration;
		rel = control.release * duration;
		sus = duration - (atk + rel);
		grAtk = Pkey(\dur) * Env.new([0.125, 0.25, 0.5, 0.75, 1.5], [0.25, 0.25, 0.25, 0.25]).at(control.density);
		grSus = Pkey(\dur) * Env.new([0.0, 0.0625, 0.125, 0.25, 0.5], [0.25, 0.25, 0.25, 0.25]).at(control.density);
		grRel = Pkey(\dur) * Env.new([0.0, 0.25, 0.5, 1.0, 2.0], [0.25, 0.25, 0.25, 0.25]).at(control.density);		amp = Env.new([0.0, 0.2, 0.6, 0.9, 1.2], [0.25, 0.25, 0.25, 0.25]).at(control.amplitude);
		surface = Env.new([0.0, 0.2, 0.5, 0.75, 0.9], [0.25, 0.25, 0.25, 0.25]).at(control.surface);
		rate = Env.new([0.25, 0.5, 1.0, 1.5, 4.0], [0.25, 0.25, 0.25, 0.25]).at(control.frequency);
		speed = Env.new([8.0, 4.0, 1.0, 0.5, 0.125], [0.25, 0.25, 0.25, 0.25]).at(control.speed);
		speedDurs = Pseq(BUtils.limitArrayByPositionSameSize(control.position, durations) * speed, inf);
		speedAmps = Pseq(BUtils.limitArrayByPositionSameSize(control.position, amplitudes) * speed, inf);
		ratePattern = Pseq(frequencies * rate, inf);
		pos = Env.new([0.0, 0.25, 0.5, 0.75, 0.9], [0.25, 0.25, 0.25, 0.25]).at(control.position);
		vib = Env.new([2, 6, 10, 14, 18], [0.25, 0.25, 0.25, 0.25]).at(control.entropy);
		vibAmt = Env.new([0.0, 0.1, 0.2, 0.3, 0.4], [0.25, 0.25, 0.25, 0.25]).at(control.entropy);
		this.rev;

	   	if(this.isPlaying > 0,
		{
			if(control.density != preControl.density, {proxyAtk.source = grAtk; proxySus.source = grSus; proxyRel.source = grRel});
			if(control.speed != preControl.speed || control.position != preControl.position, {proxyDurations.source = speedDurs; proxyAmplitudes.source = speedAmps;proxyVibFreq.source = vib});
			if(control.frequency != preControl.frequency, {proxyRate.source = ratePattern});
 			if(control.position != preControl.position, {proxyPos.source = pos});
 			if(control.entropy != preControl.entropy, {proxyVibAmt.source = vibAmt});
		},
		{
			('Potash start'.postln);
		 	proxyDurations = PatternProxy(speedDurs);
		 	proxyAmplitudes = PatternProxy(speedAmps);
		 	proxyAtk = PatternProxy(grAtk);
		 	proxySus = PatternProxy(grSus);
			proxyRel = PatternProxy(grRel);
			proxyPos = PatternProxy(pos);
			proxyRate = PatternProxy(rate);
			proxyGrAmp = PatternProxy(amp);
			proxyVibFreq = PatternProxy(vib);
			proxyVibAmt = PatternProxy(vibAmt);
			currentPattern.stop;

			pat = Pbind(\instrument, \potash,
			\amp, Pif(Ptime(inf) <= (atk+sus+rel), Env.new([0, 1, 1, 0],[atk, sus, rel], [-2,2])),
			\grAmp, proxyGrAmp,
			\pos, proxyPos,
			\rate, proxyRate,
			\dur, proxyDurations,
			\atk, proxyAtk,
			\sus, proxySus,
			\rel, proxyRel,
			\vibSpeed, proxyVibFreq,
			\vibAmt, proxyVibAmt,
			\damp, proxyAmplitudes,
			\buf, buf,
			\envbuf, -1,
			\bus, effectBus
		);

		this.playDuration(pat);
		this.isPlaying = 1;

		});

		preControl.copy(this.control);
	}

	setParamAndUpdate {|param, value|
	if(this.isPlaying > 0,
	{control.setParamValue(param, value);
	this.update.value;})}

	rev {

		reverb.set(\amp, Env.new([0.0, 0.4, 0.8, 1.5, 4], [0.25, 0.25, 0.25, 0.25]).at(control.amplitude));

		if(control.color != preControl.color,
		{
			reverb.set(\dryWet, control.color);
		});
		if(control.location != preControl.location,
		{
			reverb.set(\pan, Env.new([0.0, 0.25, 0.5, 0.75, 1.0], [0.25, 0.25, 0.25, 0.25]).at(control.location))
		});
		if(control.speed != preControl.speed,
		{
			reverb.set(\speed, Env.new([0.0, 0.25, 0.5, 0.75, 1.0], [0.25, 0.25, 0.25, 0.25]).at(control.speed))
		});
		if(control.entropy != preControl.entropy,
		{
			reverb.set(\entropy, Env.new([0.0, 0.25, 0.5, 0.75, 1.0], [0.25, 0.25, 0.25, 0.25]).at(control.entropy))
		});
		if(control.surface != preControl.surface,
		{
			reverb.set(\eq, Env.new([60, 200, 1000, 4000, 8000],[0.25, 0.25, 0.25, 0.25]).at(control.surface));
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
	var ampstep, delta, amp, steps;

		if(release > 0 && this.isPlaying == 1, {

		proxyGrAmp.source = Pif(Ptime(inf) <= (release), Env.new([control.amplitude, 0],[release], [-2]));
		delta = 0.1;
		amp = reverb.get(\amp, amp);
		steps = release / delta;
		ampstep = amp / steps;

		Routine {
		steps.do {
		amp = amp - ampstep;
		if(reverb.synth.isPlaying, {reverb.set(\amp, amp);});
		delta.wait;
		};

		currentPattern.stop;
		this.isPlaying = 0;
		}.play;

		}, {

		if(this.isPlaying == 1, { currentPattern.stop; this.isPlaying = 0;});

		});
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
		description = "BSeqPotash";
	}
}