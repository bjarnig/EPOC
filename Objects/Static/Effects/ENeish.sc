//
//ENeish : BGSoundObject
//{ 
//	var <dryWet;
//	
//	dryWet_ {arg newValue; dryWet = newValue; synth.set(\dryWet, dryWet); }
//	
//	*new { |outBus=0, inBus=1, volume=0.8, dryWet=0.5 |
//	^super.newCopyArgs(nil, outBus, inBus, volume, dryWet);
//	}
//	
//	*loadSynthDefs { 
//	SynthDef("ENeish", {|dryWet=0.1, inBus=0, outBus=0, volume=0.8|
//	var in, out;
//	in = In.ar(inBus, 1);
//	out = (in * (1-dryWet)) + (PartConv.ar(in, ~fftsize, ~irspectrum.bufnum, 0.5) * dryWet);
//	Out.ar([outBus, outBus + 1], out * volume);
//	}).add; 
//	}
//	
//	*prepare {|impulse|
//	
//	~fftsize=2048; // also 4096 works on my machine; 1024 too often and amortisation too pushed, 8192 more high load FFT
//
//	Server.local.waitForBoot {
//	{
//		var ir, irbuffer, bufsize; 
//		irbuffer= impulse;
//		Server.local.sync;
//		bufsize= PartConv.calcBufSize(~fftsize, irbuffer);
//		~irspectrum= Buffer.alloc(Server.local, bufsize, 1);
//		~irspectrum.preparePartConv(irbuffer, ~fftsize);
//		Server.local.sync;
//		irbuffer.free; // don't need time domain data anymore, just needed spectral version
//	}.fork;
//	};}
//	
//	play {
//	synth = Synth(\ENeish, [\dryWet, dryWet], addAction:\addToTail);
//	}
//	
//	free {
//	~irspectrum.free;
//	~target.free;
//	currentEnvironment.clear;	
//	}
//}

	