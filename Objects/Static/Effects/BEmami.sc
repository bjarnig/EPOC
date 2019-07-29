
BEmami : Beffect
{ 		
	*new { |values|
		^super.newCopyArgs(nil).init(values);
	}
	
	init {|values|
		wrap = Bwrap.new(\mami, values);
		this.setDescription;
	}
	
	*loadSynthDefs {
		SynthDef(\mami, {arg predelay=0.048, delaytime=0.01, decaytime=4, in=1, out=0, dryWet=0.4, lpf = 150, hpf=16000, amp=1;
		var input, signal; 
		var numc,numa,temp;
		input=In.ar(in,2); //get two channels of input starting (and ending) on bus 
		numc = 4; // number of comb delays
		numa = 6; // number of allpass delays
		temp = DelayN.ar(input, predelay, predelay);
		temp=Mix.fill(numc,{CombL.ar(temp,0.1, rrand(0.01, 0.02), decaytime)});
		numa.do({ temp = AllpassN.ar(temp, 0.051, [rrand(0.01, 0.05),rrand(0.01, 0.05)], 1) });
		temp = LPF.ar(temp, lpf) + HPF.ar(temp, hpf);
		temp = HPF.ar(temp, 80); // remove sub
		// add original sound to reverb and play it :
		signal = (temp * (dryWet)) + (input * (1 - dryWet));
		Out.ar(out, signal * amp);
		}, [0.5, 0.5, 0.5, 0.5, 0.5, 2, 2, 2]).add; 
	}
	
	play {
		wrap.play;
	}
	
		setDescription {
		description = "BEmami: Hi and lo Reverb.";
	}
}