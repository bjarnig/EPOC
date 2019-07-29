
BSeqMaya : BSeq
{
	var <>sounds, currentPattern, reverb, effectBus, isPlaying, proxyDur, proxyGrDur, proxyBuf1, proxyBuf2, proxyRate1, proxyRate2, proxyDistort, proxyLpf, proxyPitchJitter, proxyAmp, proxyPan;
	
	*new { |id=0, description, duration, control, outBus=0, sounds| 
		
		^super.newCopyArgs(id, description, duration, control, outBus, sounds, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil).init(); 
	}
	
	init {
		this.setDescription;
		isPlaying = 0;
	}
	
	*loadSynthDefs {
			 	
	 	SynthDef(\maya, 
		{|bus = 0, amp=1, pan=0, grdur=2.1, atk=0.001, rel=0.001, rate=1, offset=0, buf=0, lpf=1.0, distort=0.0, pitchJitter=0.0|
		var env = EnvGen.ar(Env.linen(atk, grdur-atk-rel, rel, 1), doneAction:2);
		var bufRate = rate + pitchJitter;
		var signal = PlayBuf.ar(1, buf, bufRate*BufRateScale.ir(buf), 1, offset*BufFrames.ir(buf), 0);
		signal = HPF.ar(signal, 70);
		signal = RLPF.ar(signal, 20000 * lpf, 1);
		signal = ((signal / SinOsc.ar(SinOsc.ar(freq:3, add:1))) * distort * (1.0-distort) * 0.5) + (signal * (1.0-distort)); 
		signal = signal * env * amp;
		OffsetOut.ar(bus, Pan2.ar(signal, pan));
		}).add; 
		
		SynthDef(\mayaVerb, {arg predelay=0.048, delaytime=0.1, decaytime=4, in=1, out=0, dryWet=0.4, lpf = 120, hpf=15000, amp=1, location=0.5;
		var input, signal; 
		var numc,numa,temp;
		input=In.ar(in, 2); //get two channels of input starting (and ending) on bus 
		numc = 4; // number of comb delays
		numa = 6; // number of allpass delays
		temp = DelayN.ar(input, predelay, predelay);
		temp=Mix.fill(numc,{CombL.ar(temp, delaytime, rrand(0.01, 0.02), decaytime)});
		numa.do({ temp = AllpassN.ar(temp, 0.051, [rrand(0.01, 0.05), rrand(0.01, 0.05)], 1) });
		temp = LPF.ar(temp, lpf) + HPF.ar(temp, hpf);
		temp = HPF.ar(temp, 85); // remove sub
		signal = (temp * (dryWet)) + (input * (1 - dryWet));
		signal = Pan2.ar(DelayC.ar(signal[0],0.2*location,0.2*location),-1,1) + Pan2.ar(DelayC.ar(signal[1],0.3*location,0.3*location),1,1);
		Out.ar(out, signal * amp);
		}, [0.5, 0.5, 0.5, 0.5, 0.5, 2, 2, 2]).add;
	}
	
	update {
				
		var pat1, pat2, halfSounds, bus1, durations, pitch, newSpeed, newAmplitude, newAttack, newRelease, newSustain, newLpf, newDistort, newGrainSizeMin, newGrainSizeMax, pitchJitter;
		
		this.control.setAttributesInRange;
		halfSounds = sounds.size/2;
		durations = List.new();
		pitch = control.frequency.linlin(0.0, 1.0, 0.1, 2);
		newAmplitude = control.amplitude; // .linlin(0.0, 1.0, 0.0, 2);
		newSpeed = control.speed;
		newAttack = control.attack * duration; 
		newRelease = control.release * duration; 
		newSustain = duration - newAttack - newRelease;
		('AMP :' ++ newAmplitude);
		
		newGrainSizeMin = control.density * 1.6 + ((-1) * control.entropy);
		newGrainSizeMax = control.density * 1.6 + (1 * control.entropy);
		pitchJitter = control.entropy; 
		
		if(control.surface < 0.5, {newLpf = control.surface; newDistort=0}, {newLpf = 1.0; newDistort=control.surface - 0.5});
		if(control.surface == 0.5, {newDistort=0; newLpf=1 });
		if(newGrainSizeMin < 0.01, {newGrainSizeMin=0.01 });
		
		while( {durations.sum < duration },
		{
			durations.addAll(Array.fill(3 + 8.rand, { (((1-newSpeed) / (2 * newSpeed)) + (1-newSpeed).rand * 0.7) }));
			durations.add((2.8 + 4.rand) * (1 - control.density));
		});
				
		this.rev;
		
		if(isPlaying > 0,
		{
			proxyDur.source = Pseq( durations, inf);
			proxyGrDur.source = Pwhite(newGrainSizeMin, newGrainSizeMax, inf);
			proxyBuf1.source = Pindex(sounds, Pbeta(0, halfSounds), inf);
			proxyBuf2.source = Pindex(sounds, Pbeta(halfSounds, sounds.size), inf);
			proxyRate1.source = Pbeta(pitch*0.8, pitch*1.1, 0.6, 0.4, inf);
			proxyRate2.source = Pbeta(pitch*0.8, pitch*1.2, 0.6, 0.4, inf);
			proxyDistort.source = newDistort;
			proxyLpf.source = newLpf;
			proxyPitchJitter.source = Pwhite(pitchJitter * (-1), pitchJitter, inf);
			proxyAmp.source = newAmplitude; // Pif(Ptime(inf) <= (duration), Env.new([0, newAmplitude, newAmplitude, 0],[newAttack, newSustain, newRelease], 'sine'));
			proxyPan.source = Pbeta(-0.3, 0.3, 0.6, 0.4);
			
			// 'MAYA: UPDATE SOURCE'.postln;
		}, 
		{
			proxyDur = PatternProxy(Pseq( durations, inf));
			proxyGrDur = PatternProxy(Pwhite(newGrainSizeMin, newGrainSizeMax, inf));
			proxyBuf1 = PatternProxy(Pindex(sounds, Pbeta(0, halfSounds), inf));
			proxyBuf2 = PatternProxy(Pindex(sounds, Pbeta(halfSounds, sounds.size), inf));
			proxyRate1 = PatternProxy(Pbeta(pitch*0.8, pitch*1.1, 0.6, 0.4, inf));
			proxyRate2 = PatternProxy(Pbeta(pitch*0.8, pitch*1.2, 0.6, 0.4, inf));
			proxyDistort = PatternProxy(newDistort);
			proxyLpf = PatternProxy(newLpf);
			proxyPitchJitter = PatternProxy(Pwhite(pitchJitter * (-1), pitchJitter, inf));
			proxyAmp = PatternProxy(Pif(Ptime(inf) <= (duration), Env.new([0, newAmplitude, newAmplitude, 0],[newAttack, newSustain, newRelease], 'sine')));
			proxyPan = PatternProxy(Pbeta(-0.3, 0.3, 0.6, 0.4));
			
			// 'MAYA: SET SOURCE'.postln;
			
				
		pat1 = Pbind(
		\instrument, 'maya',
		\dur, proxyDur,
		\grdur, proxyGrDur, 
		\buf, proxyBuf1,
		\lpf, proxyLpf,
		\distort, proxyDistort,
		\pitchJitter, proxyPitchJitter,
		\amp, proxyAmp,
		\rate, proxyRate1,
		\pan, proxyPan,
		\bus, effectBus
		);
		
		pat2 = Pbind(
		\instrument, 'maya',
		\dur, proxyDur,
		\grdur, proxyGrDur, 
		\buf, proxyBuf2,
		\lpf, proxyLpf,
		\distort, proxyDistort,
		\pitchJitter, proxyPitchJitter,
		\amp, proxyAmp,
		\rate, proxyRate2,
		\pan, proxyPan,
		\bus, effectBus
		);
		 
		this.playDuration(Ppar([pat1, pat2], 1));
		isPlaying = 1;
		});
	}
	
	rev{
		reverb.set(\dryWet,  0.35 * control.color);
		reverb.set(\delaytime, 1.1 * control.color);
		reverb.set(\decaytime, 4.5 * control.location);
		reverb.set(\location, control.location);
		reverb.set(\hipass, 17000);
		reverb.set(\lopass, 120);
	}
	
	play {		
		effectBus = Bus.audio(Server.local, 2);
		reverb =  Bwrap.new(\mayaVerb, [\in, effectBus, \out, outBus]);		reverb.play;
		this.update.value;
	}
	
	playDuration 
	{|pat|
		
		Routine 
		{
			1.do({	
		     currentPattern = pat.play;
			duration.wait;
			currentPattern.stop;
			'PATTERN STOP!'.postln; 
			3.wait;
			this.freeEffect;
			isPlaying = 0;
		})
		}.play;
	}

	stop {
		
		Routine 
		{
			1.do({	
			currentPattern.stop;
			3.wait;
			reverb.stop;
			isPlaying = 0;
		})
		}.play;
	} 
	
	setDescription {
		description = "";
	}
	
	freeEffect {
		 if(reverb.synth.isPlaying, {reverb.stop});
		 if(effectBus.index.isNil.not, {effectBus.free}); 
	} 
}