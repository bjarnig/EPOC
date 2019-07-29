
BMyua : Bmod
{ 	
	var outBus;
	
	*new { |out, values|
	^super.newCopyArgs(nil).init(out, values);
	}
	
	init {|out, values|
		outBus = out; 
		wrap = Bwrap.new(\yua, values);
		this.setDescription;
	}
		
	*loadSynthDefs { 
		SynthDef(\yua, {|outBus = 0, freq = 2, mod = 200, lfo = 0.1, amp = 0.9, 
		 valueLo=0, valueHi=1|
		var signal;
		signal = Lag.ar(LorenzL.ar(freq + (LFSaw.ar(lfo, 0.5, 1) * mod)),3e-3);
		signal = LinLin.ar(signal, -1, 1, valueLo, valueHi);
		signal = signal * amp;
		Out.kr(outBus, signal);
		}, [0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5]).add;
	}
	
	play 
	{	
		ctrlBus = Bus.control(Server.local, outBus);
		wrap.set(\out, ctrlBus);
		wrap.play;
	}
	
	setDescription {
		description = "BMyua: Chaotic Lorenz modulator.";
	}
}