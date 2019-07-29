
BSeqMarin : BSeq
{
	var <>durations, currentPattern, reverb, effectBus, proxyDur, proxyGrDur, proxyAtk, proxyRel, proxyOffset, proxyRate, proxyPan, proxyGrAmp, proxyDistort,
	proxyLpf, lastFrequency, isPlaying;
	
	*new { |id=0, description, duration, control, outBus=0, durations|
		
		^super.newCopyArgs(id, description, duration, control, outBus, durations, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil).init(); 
	}
	
	init {
		
		this.setDescription;
		isPlaying = 0;
	}
	
	*loadSynthDefs {
				
		SynthDef(\marin, 
		{|bus = 0, amp=1, pan=0, grdur=2.1, grAmp, atk=0.01, rel=0.01, freq=1, offset=0, buf=0, lop=20000, hip=20, distort=0.0, lpf=1.0|
		var env = EnvGen.ar(Env.new([0, 1, 0.2, 0],[0.4 * (grdur*0.3), grdur, grdur * 0.5]), doneAction:2);
		var signal = SinOsc.ar(freq, mul:0.2) + SinOsc.ar(freq*0.9, mul:0.2) + SinOsc.ar(freq*1.1, mul:0.2) + SinOsc.ar(freq*0.75, mul:0.2) + SinOsc.ar(freq*1.25, mul:0.2); 
		signal = RLPF.ar(signal, 20000 * lpf, 1);
		signal = (Impulse.ar(freq:freq, mul:0.75).distort * env * distort) + (signal * (1.0-distort)); 
		signal = HPF.ar(signal, 60);
		signal = signal * env * grAmp * amp;
		signal = LPF.ar(signal, lop) + HPF.ar(signal, hip); 
		OffsetOut.ar(bus, Pan2.ar(signal, pan));
		}).add; 
		
		SynthDef(\marinReso, {| outBus = 0, inBus=2, amp=1, dryWet=0.01|
		var freqs, ringtimes, input, signal; 
		freqs = Control.names([\freqs]).kr([60, 180, 400, 800, 1400, 2000, 4000, 8000, 160, 280, 500, 1800, 2400, 2500, 4500, 10000]);
		ringtimes = Control.names([\ringtimes]).kr([0.25, 0.3, 0.1, 0.4, 0.8, 1.0, 0.25, 0.7, 0.25, 0.3, 0.1, 0.4, 0.8, 1.0, 0.25, 0.7]);
		input = In.ar(inBus, 2); 
		signal = DynKlank.ar(`[ 
		freqs,		
		Array.rand(16, 0.8, 1.2),
		ringtimes 			
		], input); 
		signal = signal * 0.001 * amp * dryWet;
		signal = signal + (input * (1 - dryWet));
		Out.ar(outBus, Pan2.ar(signal, 0));
		}).add;
	
	}	
	
	update {
		
		var pat, dur, grDur, readFrom, readTo, atk, sus, rel, grAtk, grRel, amp, pan, reverb, bus1, pitchWrap, rate;
		var decayMin, decayMax, offsetPattern, ratePattern, panPattern, newDistort, newLpf, n=0;
				
		grDur = 0.001 + (0.3 * control.density);
		grAtk = (control.surface / 2) * grDur;
		grRel = grAtk;
		amp = control.amplitude;
		atk = control.attack * duration;
		rel = control.release * duration;
		sus = duration - (atk + rel);
		pan = control.location;
		readFrom = control.location;
		readTo = readFrom + control.entropy;
		offsetPattern = Pwhite(readFrom, readTo);
		panPattern = Pwhite((pan * -1), pan);
		pitchWrap = Env.new([60, 110, 200, 8000, 12000], [0.25, 0.25, 0.25, 0.25]);
		ratePattern = pitchWrap.at(control.frequency);
		dur = durations * (0.01 + ((1 - control.speed) * 3)); 
		// dur.postln;
		if(readTo > 1.0, {readTo = readFrom - control.entropy});
		if(readTo < 0.0, {readTo = 0.0});
		
		if(control.surface < 0.5, {newLpf = (control.surface + 0.01); newDistort=0}, {newLpf = 1.0; newDistort=control.surface - 0.5});
		if(control.surface == 0.5, {newDistort=0; newLpf=1 });
		
		this.rev;
		lastFrequency = control.frequency;
	   	
	   	if(isPlaying > 0,
		{
			proxyDistort.source = newDistort;
			proxyLpf.source = newLpf;
			proxyDur.source = Pseq(dur, inf) * Pwhite(rrand(1 - control.entropy, 1), 1.0); 
			proxyGrDur.source = grDur;
			proxyAtk.source = grAtk; 
			proxyRel.source = grRel;
			proxyOffset.source = offsetPattern;
			proxyRate.source = ratePattern;
			proxyPan.source = panPattern;
			proxyGrAmp.source = amp;
			
		}, 
		{
			proxyDistort = PatternProxy(newDistort);
			proxyLpf = PatternProxy(newLpf);
			proxyDur = PatternProxy(Pseq(dur, inf) * Pwhite(rrand(1 - control.entropy, 1), 1.0));
			proxyGrDur = PatternProxy(grDur); 
			proxyAtk = PatternProxy(grAtk);
			proxyRel = PatternProxy(grRel);
			proxyOffset = PatternProxy(offsetPattern);
			proxyRate = PatternProxy(ratePattern);
			proxyPan = PatternProxy(panPattern);
			proxyGrAmp = PatternProxy(amp);
					
			currentPattern.stop;
			pat = Pbind(
			\instrument, 'marin',
			\dur, proxyDur,
			\grdur, proxyGrDur,
			\atk, proxyAtk,
			\rel, proxyRel,
			\offset, 0,
			\freq, proxyRate,
			\pan, proxyPan, 
			\grAmp, proxyGrAmp,
			\amp, 1, 
			\distort, proxyDistort,
			\lpf, proxyLpf,
			\amp, Pif(Ptime(inf) <= (atk+sus+rel), Env.new([0, 1, 1, 0],[atk, sus, rel], 'sine')),
			\bus, effectBus
		);
		
		this.playDuration(pat);
		isPlaying = 1;
		
		});
	}
	
	rev {
		reverb.set(\dryWet, control.color);
		if(control.frequency != lastFrequency, {
		reverb.setn(\freqs, Array.rand(16, 60, (1000 * control.frequency).ceil));
		reverb.setn(\ringtimes, Array.rand(16, 0.08, 1.5));		
		});
	}
	
	play {
		effectBus = Bus.audio(Server.local, 2);
		reverb = Bwrap.new(\marinReso, [\inBus, effectBus, \outBus, outBus]);
		reverb.play;
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
			this.freeEffect;
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