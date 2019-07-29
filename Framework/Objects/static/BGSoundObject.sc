
BGSoundObject
{
	var <>synth, <outBus, <inBus, <amp;
	
	outBus_ {arg newValue; outBus = newValue; synth.set(\outBus, outBus); }
	inBus_ {arg newValue; inBus = newValue; synth.set(\inBus, outBus); }
	amp_ {arg newValue; amp = newValue; synth.set(\amp, amp); }
	
	*load {
	var children;
	children = BGSoundObject.subclasses;
	children.do{|item| ('BGSoundObject.loadSynthDefs: ' ++ item).postln; 
	item.loadSynthDefs};
	}
	
	*loadSynthDefs {
	}
	
	play 
	{
		{ SinOsc.ar(200, 0, 0.5) }.play;
	}
}

