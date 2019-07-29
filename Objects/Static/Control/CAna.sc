
CAna : BGSoundObject
{ 
	var <>freq, <>mod, <>lfo,  <>valueLo, <>valueHi, <>ctrlBus;
	
	*new 
	{ |outBus = 0, freq = 2, mod = 200, lfo = 0.1, amp = 0.9,  valueLo=0, valueHi=1|
	^super.newCopyArgs(nil, outBus, 0, amp, freq, mod, lfo, valueLo, valueHi);
	}
		
	*loadSynthDefs { 
		SynthDef(\CAna, {|outBus = 0, freq = 2, mod = 200, lfo = 0.1, amp = 0.9, 
		 valueLo=0, valueHi=1|
		var signal;
		signal = SinOsc.ar(freq);
		signal = LinLin.ar(signal, -1, 1, valueLo, valueHi);
		signal = signal * amp;
		Out.kr(outBus, signal);
		}).add;
	}
	
	play 
	{
		ctrlBus = Bus.control(Server.local, outBus);
		synth = Synth(\CAna, [\outBus, ctrlBus, \freq, freq, \mod, mod, \lfo, lfo, \amp, amp, 
		\valueHi, valueHi, \valueLo, valueLo]);
	}
}