
BEgoro : Beffect
{
	*new { |values|
		^super.newCopyArgs(nil).init(values);
	}

	init {|values|
		wrap = Bwrap.new(\goro, values);
		this.setDescription;
	}

	*loadSynthDefs {

	SynthDef("goro",{| Êd1 = 0.1, d2 = 0.1, d3 = 0.1, d4 = 0.1, d5 = 0.1,
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

	play {
		wrap.play;
	}

		setDescription {
		description = "BEgoro: Multi combi filter.";
	}
}