
DOran : BGSoundObject
{ 
	var <>buf, <>rate, <>trate, <>pos, <>atk, <>sus, <>rel, <>lopass, <>hipass;
	
	*new 
	{ |outBus=0, buf, rate=1, trate=12, pos=0.1, atk=4, sus=5, rel=6, amp=1, lopass=6000, hipass=80|
	^super.newCopyArgs(nil, outBus, 0, amp, buf, rate, trate, pos, atk, sus, rel, lopass, hipass);
	}
		
	*loadSynthDefs { 
	
		SynthDef(\oranGrain, 
		{|buf, rate=1, trate=12, pos=0.1, atk=4, sus=5, rel=6, amp=1, lopass=6000, hipass=80, out=0|
		var dur, clk, pan, signal, env, filtEnv;
		dur = 20 / trate;
		clk = Impulse.kr(trate);
		pos = (pos * BufDur.kr(buf)) + TRand.kr(0, 0.01, clk);
		pan = WhiteNoise.kr(0.6);
		env = EnvGen.ar(Env.new([0,1,1,0],[atk,sus,rel], -2));
		filtEnv = EnvGen.ar(Env.new([0,1,1,0],[atk/4,sus,rel/1.2], -8));
		signal = TGrains.ar(2, clk, buf, rate, pos, dur, pan, 0.1) * env * amp;
		signal = LeakDC.ar(signal);
		signal = HPF.ar(signal, hipass);
		signal = LPF.ar(signal, lopass + ((20000 - lopass) * filtEnv));
		Out.ar(out, signal);}
		).add;
	}
	
	play 
	{
		synth = Synth(\oranGrain, [\buf, buf, \pos, pos, \rate, rate, \trate, trate, \atk, atk,
		\sus, sus, \rel, rel, \amp, amp, \lopass, lopass, \hipass, hipass, \out, outBus])
	}
}