
BPume : Bpat
{
	var <>outBus, <>buf, <>atk, <>rel, <>durMin, <>durMax, <>grdur, <>readFrom, <>readTo, <>amp, <>stereoSpread, <>rate, <>grAtk, <>grRel;

	*new { |outBus=0, buf, atk=2, rel=4, durMin=0.1, durMax=0.15, grdur=0.8, readFrom=0.4, readTo=0.5, amp=1, stereoSpread=0.3, rate=1, grAtk=0.1, grRel=0.1|
		^super.newCopyArgs(nil,outBus,buf,atk,rel,durMin,durMax,grdur,readFrom,readTo,amp,stereoSpread,rate,grAtk,grRel).init();
	}

	init {
		this.setDescription;
	}

	*loadSynthDefs {

	 	SynthDef(\ume,
		{|bus = 0, amp=1, pan=0, grdur=2.1, atk=0.01, rel=0.01, rate=1, offset=0, buf=0|
		var env = EnvGen.ar(Env.linen(atk, grdur-atk-rel, rel, amp), doneAction:2);
		var signal = PlayBuf.ar(1,buf, rate*BufRateScale.ir(buf), 1, offset*BufFrames.ir(buf), 1);
		signal = HPF.ar(signal, 70);
		signal = signal * env;
		OffsetOut.ar(bus, Pan2.ar(signal, pan));
		}).add;
	}

	play {

		var pat1, pat2;

		BPume.loadSynthDefs; // TODO: Remove from here.

		pat1 = Pbind(
		\instrument, 'ume',
		\dur, Env.new([durMin, durMax, durMin],[atk, rel], 'sine'),
		\grdur, grdur,
		\buf, buf,
		\offset, Pbrown(readFrom, readTo),
		\rate, rate,
		\pan, Pwhite(stereoSpread * (-1), stereoSpread),
		\atk, grAtk,
		\atk, grRel,
		\amp, Env.new([0, amp, 0],[atk, rel], 'sine'),
		\bus, outBus
		);

		pat2 = Pbind(
		\instrument, 'ume',
		\dur, Env.new([durMin, durMax, durMin],[atk, rel], 'sine'),
		\grdur, grdur,
		\buf, buf,
		\offset, Pbrown(readFrom*0.9, readTo*1.1),
		\rate, rate * 0.99,
		\pan, Pwhite(stereoSpread * (-1), stereoSpread),
		\atk, grAtk,
		\atk, grRel,
		\amp, Env.new([0, amp/4, 0],[atk, rel], 'sine'),
		\bus, outBus
		);

		Ppar([pat1, pat2], 1).play;

	}

		setDescription {
		description = "BPsora: Pattern based brassage. Args: buf,atk,rel,durMin,durMax,grdur,readFrom,readTo,amp,stereoSpread,rate,grAtk,grRel).";
	}
}