
BPsora : Bpat
{
	var <>outBus, <>sounds, <>durations, <>amp1, <>amp2, <>grSize1, <>grSize2, <>rate1, <>rate2, <>direction;

	*new { |duration=inf, outBus=0, sounds, durations, amp1=1, amp2=0.6, grSize1=0.8, grSize2=2.1, rate1=1.6, rate2=0.4, direction=1|
		^super.newCopyArgs(nil, duration, outBus, sounds, durations, amp1, amp2, grSize1, grSize2, rate1, rate2, direction).init();
	}

	init {
		this.setDescription;

		if(direction == 0, { durations = durations.addAll(durations.reverse).addAll(durations).addAll(durations.reverse) });

		if(direction > 0, { durations = durations.addAll(durations/2).addAll(durations/4).addAll(durations*4).sort });

		if(direction > 1, { durations = durations.reverse });
	}

	*loadSynthDefs {

	 	SynthDef(\sora,
		{|bus = 0, amp=1, pan=0, grdur=2.1, atk=0.01, rel=0.01, rate=1, offset=0, buf=0|
		var env = EnvGen.ar(Env.linen(atk, grdur-atk-rel, rel, amp), doneAction:2);
		var signal = PlayBuf.ar(1,buf, rate*BufRateScale.ir(buf), 1, offset*BufFrames.ir(buf), 0);
		signal = HPF.ar(signal, 70);
		signal = signal * env;
		OffsetOut.ar(bus, Pan2.ar(signal, pan));
		}).add;
	}

	play {

		var pat1, pat2;

		BPsora.loadSynthDefs; // TODO: Remove from here.

		pat1 = Pbind(
		\instrument, 'sora',
		\dur, Pseq( durations, 1),
		\grdur, Pbeta(grSize1/2, grSize1*2, 0.6, 0.4, inf),
		\buf, Pindex(sounds, Pbrown(0, sounds.size, 1, inf)),
		\offset, 0,
		\amp, amp1,
		\pan, Pwhite(-0.6, 0.6),
		\rate, Pwhite(rate1*0.75, rate1*1.3, inf),
		\bus, outBus
		);

		pat2 = Pbind(
		\instrument, 'sora',
		\dur, Pseq( durations, 1),
		\grdur, Pbeta(grSize1/4, grSize2*4, 0.6, 0.4, inf),
		\buf, Pindex(sounds, Pbrown(0, sounds.size, 0.4, inf)),
		\offset, 0,
		\amp, amp2,
		\pan, Pwhite(-0.8, 0.8),
		\rate, Pwhite(rate2*0.75, rate2*1.3, inf),
		\bus, outBus
		);

		Ppar([pat1, pat2], 1).play;

	}

		setDescription {
		description = "BPsora: Beta patterns with sorted duration times. Args: sounds, durations amp1, amp2, grSize1, grSize2, rate1, rate2, direction(0 normal, 1 descending, 2 ascending).";
	}
}