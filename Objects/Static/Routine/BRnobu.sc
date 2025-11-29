
BRnobu : Brout
{
	var <>outBus, <>buf, <>stretch, <>stretchDev, <>durDev, <>dur, <>rateDev, <>rate, <>attack, <>sustain, <>release, <>lopassFrom, <>lopassTo, <>hipassFrom, <>hipassTo, <>amp1, <>amp2, <>amp3, <>amp4, playFunction;

	*new
	{ |outBus=0, buf=0, stretch = 15, stretchDev = 0.2, durDev = 0.2, dur = 0.2, rateDev = 0.02,
	rate = 1, attack=4, sustain=3, release=10, lopassFrom = 50, lopassTo=800, hipassFrom=800, 	hipassTo=20000, amp1 = 2, amp2 = 1, amp3 = 1, amp4 = 1|		^super.newCopyArgs(nil, outBus,buf,stretch,stretchDev,durDev,dur,rateDev,
		rate,attack,sustain,release,lopassFrom,lopassTo, hipassFrom, hipassTo, amp1, amp2,
		amp3, amp4).init();
	}

	init {

	playFunction = {arg outBus = 0, stretch = 1, dur = 0.06, rate = 1, amp = 0.5, 		attack=0.5, sustain=2, release=2, lopassFrom=20000, lopassTo=20000, hipassFrom=20, 		hipassTo=20;

		Routine({
		var next, st, pos, gdur, ovlp, grainCount, totaldur, durEnv, ampEnv, ampVal, lopass, 		hipass;
		ovlp = 2;
		totaldur = buf.numFrames / buf.sampleRate;
		grainCount = ((totaldur / dur) * ovlp * stretch).asInteger;
		durEnv = Env.new([dur/2, dur, dur*0.8, dur/4, dur/8],[totaldur/4,totaldur/2,totaldur/4, 		totaldur/16], 'linear');
		ampEnv = Env.new([0.0, 1.0, 1.0, 0.0], [attack, sustain, release], 'linear');
		grainCount.do({ arg i;
			next = dur / ovlp;
			pos 	= rrand(-0.3,0.3);
			gdur = durEnv.at((i/grainCount) * totaldur);
			ampVal = (ampEnv.at((i * next))) * amp;
			st 	= i * next / totaldur / stretch;
			hipass = rrand(hipassFrom, hipassTo);
			lopass = rrand(lopassFrom, lopassTo);
			Server.internal.makeBundle(0.1, { Synth("nobu", [\buf, buf.bufnum,
			\dur, gdur, \rate, rate, \start, st, \pan, pos, \amp, ampVal, \hip, hipass,
			\lop, lopass, \buf, buf, \outBus, outBus]) });
		next.wait;
	});
	})};
	}

	*loadSynthDefs {

		SynthDef("nobu", { arg buf=0, rate=1, start=0, outBus=0, amp=0.2, dur=0.3, pan=0,
		hip=30, lop=20000;
		var env, sig;
		env = EnvGen.ar(Env([0, amp, 0], [dur*0.5, dur*0.5], \sine), doneAction: 2);
		sig = PlayBuf.ar(1, buf, rate * BufRateScale.ir(buf), 1, start * BufSamples.ir(buf), 0);
		sig = BHiPass.ar(sig, hip) +  BLowPass.ar(sig, lop);
		sig = HPF.ar(sig, 80);
		sig = Pan2.ar(sig * env * 0.5, pan);
		OffsetOut.ar(outBus, sig);
		}).add;
	}

	play
	{
		playFunction.value(outBus:outBus, stretch:stretch, dur:dur, rate:rate, 		amp:amp1, lopassFrom:lopassFrom, lopassTo:lopassTo, 		hipassFrom:hipassFrom,hipassTo:hipassTo,attack:attack, sustain:sustain, 		release:release).play;

		playFunction.value(outBus:outBus, stretch:((stretchDev * 1) + 1) * stretch, 		dur:	(durDev * 1) + dur, rate: ((rateDev * 	2) + rate), amp:amp2, lopassFrom:lopassFrom, 		lopassTo:lopassTo, hipassFrom:hipassFrom, hipassTo:hipassTo, attack:attack, 		sustain:sustain, release:release).play;

		playFunction.value(outBus:outBus, stretch:((stretchDev * 2) + 1) * stretch, 		dur: (durDev * 2) + dur, rate:((rateDev * 3) + rate), amp:amp3, lopassFrom:lopassFrom, 		lopassTo:lopassTo, hipassFrom:hipassFrom, hipassTo:hipassTo, attack:attack, 		sustain:sustain, release:release).play;

		playFunction.value(outBus:outBus, stretch:((stretchDev * 3) + 1) * stretch, 		dur: (durDev * 3) + dur, rate:((rateDev * 4) + rate), amp:amp4, lopassFrom:lopassFrom, 		lopassTo:lopassTo, 	hipassFrom:hipassFrom, hipassTo:hipassTo, attack:attack, 		sustain:sustain, release:release).play;
	}
}

