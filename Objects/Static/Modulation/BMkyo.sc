
BMkyo : Bmod
{ 	
	var outBus;
	
	*new { |out, values|
	^super.newCopyArgs(nil).init(out, values);
	}
	
	init {|out, values|
		outBus = out; 
		wrap = Bwrap.new(\kyo, values);
		this.setDescription;
	}
		
	*loadSynthDefs { 
		SynthDef(\kyo, {| out = 0, bufnum = 0, amp = 10, amAtk = 0.5, amRel = 0.5, valueLo=100, valueHi=200|
		var mod, ampMod, lag, clip, signal;
		mod = PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum), loop:1);
		ampMod = Amplitude.ar(mod, amAtk, amRel) * amp;
		clip = Clip.ar(ampMod, 0, 1);
		lag = Lag.ar(clip, 0.2);
		signal = LinLin.ar(lag, 0, 1, valueLo, valueHi);
		Out.kr(out, signal);
		}, [0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5]).add;
	}
	
	play 
	{	
		ctrlBus = Bus.control(Server.local, outBus);
		wrap.set(\out, ctrlBus);
		wrap.play;
	}
	
	setDescription {
		description = "BMkyo: An amplitude modulator. A Buffer ref is needed. Amplitude tends to get low, it boosted sincs it is later clipped.";
	}
}