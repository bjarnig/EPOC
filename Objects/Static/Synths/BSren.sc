
BSren : Bsynth
{ 		
	*new { |values|
		^super.newCopyArgs(nil).init(values);
	}
	
	init {|values|
		wrap = Bwrap.new(\ren, values);
		this.setDescription;
	}
	
	*loadSynthDefs {
		
	 	SynthDef(\ren, 
		{|out = 0, buf, rate=1, trate=12, pos=0.1, atk=4, sus=50, rel=6, amp=1, lopass=5000, hipass=80|
		var dur, clk, pan, signal, env, filtEnv;
		dur = 20 / trate;
		clk = Impulse.kr(trate);
		pos = (pos * BufDur.kr(buf)) + TRand.kr(0, 0.01, clk);
		pan = WhiteNoise.kr(0.4);
		env = EnvGen.ar(Env.new([0,1,1,0],[atk,sus,rel], -2));
		filtEnv = EnvGen.ar(Env.new([0,1,1,0],[atk/4,sus,rel/1.2], -8));
		signal = TGrains.ar(2, clk, buf, rate, pos, dur, pan, 0.1) * env * amp;
		signal = LeakDC.ar(signal);
		signal = HPF.ar(signal, hipass);
		signal = LPF.ar(signal, lopass + ((20000 - lopass) * filtEnv));
		Out.ar(out, signal);}, [0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1] 
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
		description = "BSren: Static granular player.";
	}
}