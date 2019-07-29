
BPassage : Bpat
{
	var <>outBus, <>buf, <>atk, <>rel, <>dur, <>grdur, <>readFrom, <>readTo, <>amp, <>rate;
	
	*new { |duration=inf, outBus=0, buf, atk=2, rel=4, dur=0.2, grdur=0.8, readFrom=0.1, readTo=0.5, amp=1, rate=1| 
		^super.newCopyArgs(nil,duration, outBus,buf,atk,rel,dur,grdur,readFrom,readTo,amp,rate).init(); 
	}
	
	init {
		this.setDescription;
	}
	
	*loadSynthDefs {

		SynthDef(\assage, 
		{|bus = 0, amp=1, pan=0, grdur=2.1, atk=0.01, rel=0.01, rate=1, offset=0, buf=0, lop=20000, hip=20|
		var env = EnvGen.ar(Env.linen(atk, grdur-atk-rel, rel, amp), doneAction:2);
		var signal = PlayBuf.ar(1,buf, rate*BufRateScale.ir(buf), 1, offset*BufFrames.ir(buf), 1);
		signal = HPF.ar(signal, 60);
		signal = signal * env;
		signal = LPF.ar(signal, lop) + HPF.ar(signal, hip); 
		OffsetOut.ar(bus, Pan2.ar(signal, pan));
		}).add; 
	}
	
	play {
				
		var pat1;
				
		BPassage.loadSynthDefs; // TODO: Remove from here.
		
		pat1 = Pbind(
		\instrument, 'assage',
		\dur, dur, 
		\grdur, grdur,
		\buf, buf,
		\offset, Pbrown( readFrom,  readTo),
		\rate,  Pwhite(0.99 * rate, rate*1.01),
		\pan, Pwhite(-0.7, 0.7), 
		\amp, Pif(Ptime(inf) <= (duration+atk+rel), Env.new([0, amp, amp, 0],[atk, duration, rel], 'sine'))  ,
		\bus, outBus
		);
			
		pat1.play;
	}
	
		setDescription {
		description = "BPassage: Buffer based brassage. Args: buf,atk,rel,durMin,durMax,grdur,readFrom,readTo,amp,stereoSpread,rate,grAtk,grRel).";
	}
}