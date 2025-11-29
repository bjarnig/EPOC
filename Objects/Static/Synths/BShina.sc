
BShina : Bsynth
{
	*new { |values|
		^super.newCopyArgs(nil).init(values);
	}

	init {|values|
		wrap = Bwrap.new(\hina, values);
		this.setDescription;
	}

	*loadSynthDefs {

	 	SynthDef(\hina,
		{| freq = 440, mod = 2, out = 1|
	 	Out.ar(out, SinOsc.ar(SinOsc.ar(mod, 0.5, 1) * freq));
		}, [1, 1, 1]
		).add;
	}

	addIntensity {
		wrap.set(\freq, wrap.get(\freq) * 1.2);
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
		description = "BShina: Sine wave with modulation for test.";
	}
}