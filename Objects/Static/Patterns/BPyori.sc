
BPyori : Bpat
{
	var <>outBus, <>sounds, <>durations, <>amp1, <>amp2, <>rate1, <>rate2, <>grDurMin, <>grDurMax, <>speed1, <>speed2, <>centerFreq, <>bw;
	
	*new { |duration=inf, outBus=0, sounds, durations, amp1=1, amp2=0.6, rate1=1, rate2=0.5, grDurMin=0.4, grDurMax=1.4, speed1=1, speed2=1, centerFreq=1000, bw=0.1| 
		^super.newCopyArgs(nil, duration, outBus, sounds, durations, amp1, amp2, rate1, rate2, grDurMin, grDurMax, speed1, speed2, centerFreq, bw).init(); 
	}
	
	init {
		this.setDescription;
	}
	
	*loadSynthDefs {

	 	SynthDef(\yori, 
		{|bus = 0, amp=1, pan=0, grdur=2.1, atk=0.01, rel=0.01, rate=1, offset=0, buf=0, centerFreq=1000, bw=0.1|
		var env = EnvGen.ar(Env.linen(atk, grdur-atk-rel, rel, amp), doneAction:2);
		var signal = PlayBuf.ar(1,buf, rate*BufRateScale.ir(buf), 1, offset*BufFrames.ir(buf), 0);
		signal = HPF.ar(signal, 70);
		signal = BBandPass.ar(signal, centerFreq, bw);
		signal = signal * env;
		OffsetOut.ar(bus, Pan2.ar(signal, pan));
		}).add; 
	}
	
	play {
				
		var pat1, pat2;
	
		pat1 = Pbind(
		\instrument, 'yori',
		\dur, Prand(durations * speed1, inf), 		
		\grdur, Pwhite(grDurMin, grDurMax, inf),
		\buf, Pxrand(sounds, inf),
		\offset, 0,
		\pan, Pwhite(-0.2, 0.2),
		\amp, amp1,
		\rate, rate1,
		\bus, outBus,
		\centerFreq, centerFreq,
		\bw, bw
		);
		
		pat2 = Pbind(
		\instrument, 'yori',
		\dur, Prand(durations * speed2, inf),
		\grdur, Pwhite(grDurMin, grDurMax, inf),
		\buf, Pxrand(sounds, inf),
		\pan, Pwhite(-0.6, 0.6),
		\offset, 0,
		\amp, amp2,
		\rate, rate2, 
		\bus, outBus,
		\centerFreq, centerFreq,
		\bw, bw
		);
		
		this.playDuration(Ppar([pat1, pat2], 1));
	}
	
		setDescription {
		description = "BPyori: Stochastic patterns with bandpass filtering.";
	}
}