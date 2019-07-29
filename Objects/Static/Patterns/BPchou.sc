
BPchou : Bpat
{
	var <>outBus, <>amp, <>speed, <>brownStep, <>durFrom, <>durTo, <>sampleStart, 
	<>sampleEnd, <>grainAttack, <>grainRelease, <>cloudAttack, <>cloudRelease, <>rate, <>buffer;
	
	*new { |outBus=0, amp=0.7, speed=0.1, brownStep=0.001, durFrom=0.1, durTo=1.5, 	sampleStart=0.0, sampleEnd=0.4, grainAttack=0.01, grainRelease=0.2, cloudAttack=8, 	cloudRelease=12, rate=1, buffer=0| 
		^super.newCopyArgs(nil, outBus, amp, speed, brownStep, durFrom, durTo, sampleStart, 
		sampleEnd, grainAttack, grainRelease, cloudAttack, cloudRelease, rate, buffer).init(); 
	}
	
	init {
		this.setDescription;
	}
	
	*loadSynthDefs {
		
	 	SynthDef(\chou, 
		{|bus = 0, amp=0.1, pan=0, grdur=0.1, atk=0.01, rel=0.1, rate=1, offset=0, buf|
		var env = EnvGen.ar(Env.linen(atk, grdur-atk-rel, rel, amp), doneAction:2);
		var signal = PlayBuf.ar(1,buf, rate*BufRateScale.ir(buf), 1, offset*BufFrames.ir(buf), 0);
		signal = signal * env;
		signal = HPF.ar(signal, 80);
		OffsetOut.ar(bus, Pan2.ar(signal, pan + rrand(-0.3, 0.3)));
		}).add; 
	}
	
	play {
		
		var pat1, pat2, pat3, pat4;
	
		pat1 = Pbind(
		\instrument, 'chou',
		\dur, Env.new([speed/2, speed, speed * 2], [cloudAttack*1.2, cloudRelease], 'linear'),
		\grdur, Pbrown(durFrom, durTo, brownStep),
		\buf, buffer,
		\offset, Pbrown(sampleStart, sampleEnd, brownStep),
		\atk, grainAttack,
		\rel, grainRelease,
		\amp, Env.linen(cloudAttack, cloudRelease/2, cloudRelease, 0.4*amp, 5),
		\pan, Pbeta(-0.2, 0.2, 0.45, 0.55),
		\rate, rate
		);

		pat2 = Pbind(
		\instrument, 'chou',
		\dur, speed/2,
		\grdur, Pbrown(durFrom + 0.1.rand, durTo+0.1.rand, brownStep),
		\buf, buffer,
		\offset, Pbrown(sampleStart, sampleEnd + 0.1, brownStep),
		\atk, grainAttack + 0.01,
		\rel, grainRelease + 0.02,
		\amp, Env.linen(cloudAttack + 8,0, cloudRelease + (cloudRelease/32), 0.2*amp, 4),
		\pan, Pbeta(-0.2, 0.2, 0.45, 0.55),
		\rate, rate - 0.2
		);

		pat3 = Pbind(
		\instrument, 'chou',
		\dur, Env.new([speed/4, speed/2, speed], [cloudAttack*1.4, cloudRelease*0.8], 'linear'),
		\grdur, Pbrown(durFrom/2, durTo/2, brownStep),
		\buf, buffer,
		\offset, Pbrown(sampleStart, sampleEnd + 0.2, brownStep),
		\atk, grainAttack + 0.03,
		\rel, grainRelease + 0.04,
		\amp, Env.linen(cloudAttack + 6,0, cloudRelease + (cloudRelease/16), 0.3*amp, 4),
		\pan, Pbeta(-0.4, 0.4, 0.6, 0.4),
		\rate, rate + 0.2
		);

		pat4 = Pbind(
		\instrument, 'chou',
		\dur, speed*4,
		\grdur, Pbrown(durFrom, durTo, brownStep),
		\buf, buffer,
		\offset, Pbrown(sampleStart, 1, brownStep),
		\atk, grainAttack - 0.01,
		\rel, grainAttack - 0.02,
		\amp, Env.linen(cloudAttack + 8, 0, cloudRelease + (cloudRelease/8), 0.1*amp, 2),
		\pan, Pbeta(-0.4, 0.4, 0.6, 0.4),
		\rate, rate/2
		);
	
		Ppar([pat1, pat2, pat3, pat4]).play;
	}
	
		setDescription {
		description = "BPchou: Pattern based granulation.";
	}
}