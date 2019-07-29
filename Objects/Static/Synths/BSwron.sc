
BSwron : Bsynth
{ 		
	*new { |values|
		^super.newCopyArgs(nil).init(values);
	}
	
	init {|values|
		wrap = Bwrap.new(\wron, values);
		this.setDescription;
	}
	
	*loadSynthDefs {
		
	SynthDef(\wron, {|buffer=0, duration=15, outBus=0, pitch=0.8, dir=1, atk=0.1, rel=0.9, amp=0.8|
	var out, pointer, filelength, env;
	pointer = Line.kr(1-dir, dir, duration);
	env = EnvGen.kr(Env([0.001, 1, 1, 0.001], [atk, duration-atk-rel, rel], 'exp'), doneAction: 2);
	out = Warp1.ar(1, buffer, pointer, pitch, 0.1, -1, 2, 0.1, 2);
	Out.ar(outBus, Pan2.ar(out * env, 0) * amp);
	}).add;
	}
	
	play {| length |
	
		if(length.notNil){
			Routine {
			1.do {
			wrap.play;
			length.wait;
			wrap.synth.free;
			}}.play;
		}{ 
			wrap.play;
		}
	}
	
		setDescription {
		description = "BSwron: Wrap granular buffer. Params: buffer, duration, outBus, pitch, dir, atk, rel";
	}
}