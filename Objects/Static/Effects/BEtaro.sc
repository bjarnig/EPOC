
BEtaro : Beffect
{ 		
	*new { |values|
		^super.newCopyArgs(nil).init(values);
	}
	
	init {|values|
		wrap = Bwrap.new(\taro, values);
		this.setDescription;
	}
	
	*loadSynthDefs {
		
	SynthDef(\taro, {| outBus = 0, inBus=2, amp=1, dryWet=0.8|
	var freqs, ringtimes, input, signal; 
	freqs = Control.names([\freqs]).kr(Array.rand(128, 100.0, 5000));
	ringtimes = Control.names([\ringtimes]).kr(Array.rand(128, 4, 12));
	input = In.ar(inBus, 2); 

	signal = DynKlank.ar(`[ 
		freqs,		
		Array.rand(128, 0.001, 0.9),
		ringtimes 			
		], input); 

	signal = signal * 0.001 * amp * dryWet;
	signal = signal + (input * (1 - dryWet));
	Out.ar(outBus, Pan2.ar(signal, 0));
	}).add;
	}
	
	play {
		wrap.play;
	}
	
	setFreqs {
	|freqMin=300, freqMax=2000|
		wrap.synth.setn(\freqs, Array.rand(128, freqMin, freqMax));
	}
	
	setDecays {
	|decayMin=0.3, decayMax=2|
		wrap.synth.setn(\ringtimes, Array.rand(256, decayMin, decayMax));
	}
	
	setDescription {
		description = "BEtaro: 128 resonating frequencies. outBus = 0, inBus = 1, amp"; 
	}
}