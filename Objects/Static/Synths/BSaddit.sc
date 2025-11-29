
BSaddit : Bsynth
{
	*new { |values|
		^super.newCopyArgs(nil).init(values);
	}

	init {|values|
		wrap = Bwrap.new(\addit, values);
		this.setDescription;
	}

	*loadSynthDefs {

	 	SynthDef(\addit,
		{| freq1=51, freq2=98, freq3=141, freq4=225, amp1=0.25, amp2=0.25, amp3=0.15, amp4=0.15, atk=0.5, sus=4, rel=4|
	 	var env = EnvGen.kr(Env.new([0,1,1,0], [atk,sus,rel], -2), doneAction:2);
	 	var osca = SinOsc.ar(freq1, mul:amp1);
		var oscb = SinOsc.ar(freq2, mul:amp2);
		var oscc = SinOsc.ar(freq3, mul:amp3);
		var oscd = SinOsc.ar(freq4, mul:amp4);
		var signal = (osca + oscb + oscc + oscd);
		signal = signal * env * 0.6;
		signal = Pan2.ar(signal, 0);
		Out.ar(0, signal);
		}, [0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1]
		).add;
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
		description = "BSaddit: 4 part additive synthesis";
	}
}