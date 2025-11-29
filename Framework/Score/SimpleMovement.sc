SimpleMovement{
	var <func, <time, <endFunc, <gran, <loopback, <startTime;
	var <routine;

	*new{ |func,time,endFunc,gran = 0.1,loopback = false,startTime =0|
		^super.newCopyArgs(func,time,endFunc, gran, loopback,startTime).start;
	}

	*basicNew{ |func,time,endFunc,gran = 0.1,loopback =false|
		^super.newCopyArgs(func,time,endFunc,gran,loopback).doMov;
	}

	stop{
		routine.stop
	}

	start{
		routine = Routine({
			if(loopback){
				this.doMovLoop
			}{
				this.doMov
			}
		}).play(SystemClock)

	}

	doMov{
		var spec = [0,time/gran-1].asSpec;					if(startTime >= time){ startTime = 0 }; //check for invalid startTimes.

		((time-startTime)/gran).asInteger.do{ |i|
			var x = spec.unmap(i+(startTime/gran));
			{ func.value(x) }.defer;
			gran.wait;
		};
		endFunc.value;
		// "SimpleMovement done".postln;
	}

	doMovLoop{
		var spec = [0,time/gran-1].asSpec;
		var inverseSpec = [time/gran-1,0].asSpec;
		var foward = true;
		var usespec;
		inf.do{
			if(foward){
				usespec = spec;
			}{
				usespec = inverseSpec;
			};
			(time/gran).do{ |i|
				var x = usespec.unmap(i);
				{ func.value(x) }.defer;
				gran.wait;
			};
			foward = foward.not;
		}

	}



}

Crossfade{

	*new{ |a,b|
		var spec = [a,b,\linear].asSpec;
		^{ |t| spec.map(t) }
	}
}

SimpleScore : SimpleMovement {
	var <tracks, <simpleMovement;

	*new{ |tracks|
		var totalTime = tracks.collect{ |track| track[1]+track[2] }.maxItem;
		var relativeStartTimes = tracks.collect{ |track| track[1]/totalTime };
		var relativeEndTimes = tracks.collect{ |track| (track[1]+track[2])/totalTime };
		var convertSpecs = tracks.collect{ |track,i| [relativeStartTimes[i],relativeEndTimes[i],\linear].asSpec };
		var func = { |t|
			tracks.do{ |track,i|
				var func, startTime,duration;
				#func,startTime,duration = track;
				if( (t > relativeStartTimes[i]) && ( t < relativeEndTimes[i])){
					func.value(convertSpecs[i].unmap(t))
				}

			}
		};
		^super.new(func,totalTime)
	}


}
