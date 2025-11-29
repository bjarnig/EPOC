
EFairlie : BGSoundObject
{
	var <delay1, <delay2, <delay3, <delay4, <delay5, <decay1, <decay2, <decay3, <decay4, <decay5,
	<lopass1, <lopass2, <lopass3, <lopass4, <lopass5, <delayMult, <decayMult, <filtMult;

	delay1_ {arg newValue; delay1 = newValue; synth.set(\d1, delay1); }
	delay2_ {arg newValue; delay2 = newValue; synth.set(\d2, delay2); }
	delay3_ {arg newValue; delay3 = newValue; synth.set(\d3, delay3); }
	delay4_ {arg newValue; delay4 = newValue; synth.set(\d4, delay4); }
	decay1_ {arg newValue; decay1 = newValue; synth.set(\t1, decay1); }
	decay2_ {arg newValue; decay2 = newValue; synth.set(\t2, decay2); }
	decay3_ {arg newValue; decay3 = newValue; synth.set(\t3, decay3); }
	decay4_ {arg newValue; decay4 = newValue; synth.set(\t4, decay4); }
	lopass1_ {arg newValue; lopass1 = newValue; synth.set(\f1, lopass1); }
	lopass2_ {arg newValue; lopass2 = newValue; synth.set(\f2, lopass2); }
	lopass3_ {arg newValue; lopass3 = newValue; synth.set(\f3, lopass3); }
	lopass4_ {arg newValue; lopass4 = newValue; synth.set(\f4, lopass4); }
	delayMult_ {arg newValue; delayMult = newValue; synth.set(\delayMult, delayMult); }
	decayMult_ {arg newValue; decayMult = newValue; synth.set(\decayMult, decayMult); }
	filtMult_ {arg newValue; filtMult = newValue; synth.set(\filtMult, filtMult); }

	*new { |inBus=1, outBus=0, amp = 0.8, delay1 = 0.1, delay2 = 0.15, delay3 = 0.2,
	delay4 = 0.25, delay5 = 0.3, decay1 = 1, decay2 = 1.25, decay3 = 1.5, decay4 = 1.75,
	decay5 = 2, lopass1 = 20000, lopass2 = 20000, lopass3 = 20000, lopass4 = 20000,
	lopass5 = 20000, delayMult = 1.0, decayMult = 1.0, filtMult = 1.0|

	^super.newCopyArgs(nil, outBus, inBus, amp, delay1, delay2, delay3, delay4, delay5, decay1, 	decay2, decay3, decay4, decay5, lopass1, lopass2, lopass3, lopass4, lopass5, delayMult, 	decayMult, filtMult);
	}

	*loadSynthDefs {

	SynthDef("FairlieComber",{| Êd1 = 0.1, d2 = 0.1, d3 = 0.1, d4 = 0.1, d5 = 0.1,
Ê Ê Êt1 = 1, t2 = 2, t3 = 3, t4 = 4, t5 = 5, f1 = 20000, f2 = 20000, f3 = 20000, f4 = 20000,
	f5 = 20000, inBus = 3, outBus = 0, amp=0.8, delayMult=1.0, decayMult=1.0, filtMult=1.0|
Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê
Ê Ê Ê	var in, out, c1, c2, c3, c4, c5;
		in = In.ar(inBus, 1);

	  	c1 = LPF.ar(CombC.ar(in, 1, d1 * delayMult, t1 * decayMult), f1 * filtMult);
Ê Ê Ê Ê	c2 = LPF.ar(CombC.ar(in, 1, d2 * delayMult, t2 * decayMult), f2 * filtMult);
Ê Ê Ê Ê	c3 = LPF.ar(CombC.ar(in, 1, d3 * delayMult, t3 * decayMult), f3 * filtMult);
Ê Ê Ê Ê	c4 = LPF.ar(CombC.ar(in, 1, d4 * delayMult, t4 * decayMult), f4 * filtMult);
Ê Ê Ê Ê	c5 = LPF.ar(CombC.ar(in, 1, d5 * delayMult, t5 * decayMult), f5 * filtMult);
Ê Ê Ê Ê
Ê Ê Ê Ê	out = (c1 + c2 + c3 + c4 + c5) * 0.2;

Ê Ê Ê Ê	Out.ar([outBus, outBus + 1], out * amp);

	}).add;
	}

	play
	{
		synth = Synth(\FairlieComber, [\outBus, outBus, \inBus, inBus, \amp, amp, \d1, 		delay1, \d2, delay2, \d3, delay3, \d4, delay4,\t1, decay1, \t2, decay2, \t3, decay3, \t4, 		decay4, \f1, lopass1, \f2, lopass2, \f3, lopass3, \f4, lopass4,\decayMult, decayMult, 		\delayMult, delayMult, \filtMult, filtMult], addAction:\addToTail);
	}
}

