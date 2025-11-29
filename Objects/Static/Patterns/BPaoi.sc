
BPaoi : Bpat
{
	var <>outBus, <>sounds, <>durations, <>amp1, <>amp2, <>grSize1, <>grSize2, <>maxRate, <>minRate, <>direction;

	*new { |duration=inf, outBus=0, sounds, durations, amp1=1, amp2=0.6, grSize1=0.8, grSize2=2.1, maxRate=1.6, minRate=0.4, direction=1|
		^super.newCopyArgs(nil, duration, outBus, sounds, durations, amp1, amp2, grSize1, grSize2, maxRate, minRate, direction).init();
	}

	init {
		this.setDescription;

		if(direction == 0, { durations = durations.addAll(durations.reverse).addAll(durations).addAll(durations.reverse) });

		if(direction > 0, { durations = durations.addAll(durations/2).addAll(durations/4).addAll(durations*4).sort });

		if(direction > 1, { durations = durations.reverse });
	}

	*loadSynthDefs {

	 	SynthDef(\aoi,
		{|bus = 0, amp=1, pan=0, grdur=2.1, atk=0.01, rel=0.01, rate=1, offset=0, buf=0|
		var env = EnvGen.ar(Env.linen(atk, grdur-atk-rel, rel, amp), doneAction:2);
		var signal = PlayBuf.ar(1,buf, rate*BufRateScale.ir(buf), 1, offset*BufFrames.ir(buf), 0);
		signal = HPF.ar(signal, 70);
		signal = signal * env;
		OffsetOut.ar(bus, Pan2.ar(signal, pan));
		}).add;
	}

	play {

		var pat1, pat2, halfSounds;
		halfSounds = sounds.size/2;

		BPaoi.loadSynthDefs; // TODO: Remove from here.

		pat1 = Pbind(
		\instrument, 'aoi',
		\dur, Pseq( durations, inf),
		\grdur, grSize1,
		\buf, Pindex(sounds, Pbeta(0, halfSounds)),
		\offset, 0,
		\amp, amp1,
		\rate, Pbeta(minRate, maxRate, 0.6, 0.4, inf),
		\pan, Pbeta(-0.3, 0.3, 0.6, 0.4),
		\bus, outBus
		);

		pat2 = Pbind(
		\instrument, 'aoi',
		\dur, Pseq( durations, inf),
		\grdur, grSize2,
		\buf, Pindex(sounds, Pbeta(halfSounds, sounds.size)),
		\offset, 0,
		\amp, amp2,
		\rate, Pbeta(minRate, maxRate, 0.6, 0.4, inf),
		\pan, Pbeta(-0.3, 0.3, 0.4, 0.6),
		\bus, outBus
		);

		this.playDuration(Ppar([pat1, pat2], 1));

	}

		setDescription {
		description = "BPaoi: Beta patterns with sorted duration times. Args: sounds, durations, amp1, amp2, grSize1, grSize2, maxRate, <>minRate, direction(0 normal 1 descending, 2 ascending).";
	}
}