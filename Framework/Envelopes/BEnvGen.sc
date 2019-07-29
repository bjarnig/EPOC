
BEnvGen
{	
	*getEnv {arg time=10, type, curve=1,from=0.0, to=1.0;
	var env = Env.new([from, to], [time], [curve]);  
	
	if(type == "ASCENT", {env = Env.new([from, to], [time], [curve]);});
	if(type == "DESCENT", {env = Env.new([from, to], [time], [curve]);});
	if(type == "PARABOLA", {env = Env.new([from, to, from], [time/2, time/2], [curve * (-1), curve]);}); 
	if(type == "OSCILLATION", {env = this.getOscillation(time, from, to, curve, curve)}); 
	if(type == "UNDULATION", {env = this.getOscillation(time, from, to, curve * -4, curve * (-4))}); 
	if(type == "RANDOM", {env = this.getRandom(time, from, to, curve * -4, curve * (-4))}); 
	if(type == "SQUARE", {env = this.getSquare(time, from, to, curve * -4, curve * (-4))}); 
	
	^env;
	
	}
	
	*getOscillation {
	arg time=10, from=0, to=1, curve=1, curve2=1;  
	var points, values, times, valuePoints, timePoints; 

	points = time / 2;
	times = Array.new(points-1);
	values = Array.new(points);
	valuePoints = ((points / 2).ceil);
	timePoints = (points-1);

	valuePoints.do {
	values.add(from);
	values.add(to);
	};

	timePoints.do {
	times.add(time/points);
	};

	^Env.new(values, times, [curve, curve2]);
	
	}
	
	*getSquare { arg time=50, from=0.2, to=0.8, curve= 8, curve2= 8;  
	var points, values, times, valuePoints, timePoints; 

	points = time / 2;
	times = Array.new(points-1);
	values = Array.new(points);
	valuePoints = ((points / 4).ceil);
	timePoints = ((valuePoints * 2) -1);

	valuePoints.do {

	values.add(from);
	values.add(to);
	values.add(to);
	values.add(from);
	
	};

	timePoints.do {

	times.add(0.01);
	times.add(time/points);

	};

	^Env.new(values, times, [curve, curve2]);

	}
	
	*getRandom {
	arg time=10, from=0, to=1, curve=1, curve2=1;  
	var points, values, times, timePoints; 

	points = time * 10;
	times = Array.new(points-1);
	values = Array.new(points);
	timePoints = (points-1);
	values.add(from);
	
	points.do {
	values.add(rrand(from, to));
	};
	
	values.add(to);

	timePoints.do {
	times.add(time/points);
	};
	
	times.add(time/points);

	^Env.new(values, times, [curve, curve2]);
	
	}
	
	*combine {arg enva, envb;
	var times, levels, curves, envc;

	times = Array.new(enva.times.size + envb.times.size);
	levels = Array.new(enva.times.size + envb.times.size);
	curves = Array.new(enva.times.size + envb.times.size);
	times.add(enva.times);	
	times.add(envb.times);
	levels.add(enva.levels);	
	levels.add(envb.levels);	
	curves.add(enva.curves);
	curves.add(envb.curves);

	^Env.new(levels.flatten, times.flatten, curves.flatten);   
	}
	
	*combineArray {arg arraya, arrayb;
	var output;

	output = Array.new(arraya.size + arrayb.size);
	output.add(arraya);
	output.add(arrayb);

	^output.flatten;
	}
	
	*copyEnvelope {arg env;
	^Env.new(env.levels, env.times, env.curves);
	}
	
	*reverseTimes { arg env;
	env.times = env.times.reverse;
	^env;
	}
	
	*reverseLevels { arg env;
	env.levels = env.levels.reverse;
	^env;
	}
	
	*mirrorTimes { arg env;
	env.times = env.times.mirror;
	^env;
	}
	
	*mirrorLevels { arg env;
	env.levels = env.levels.mirror;
	^env;
	}
	
	*scaleLevels { arg env, factor;
	var n=0;
	env.levels = env.levels * factor;
	
	env.levels.do{arg item;
		if(item > 1.0, {env.levels[n] = 1.0});
		n = n + 1;
		};
	
	^env;
	}
	
	*addLevels { arg env, factor;
	var n=0;
	
	env.levels.do{arg item;
		if((item + factor) > 1.0, {env.levels[n] = 1.0}, {env.levels[n] = env.levels[n] + factor});
		n = n + 1;
		};
	
	^env;
	}
	
	*scaleTimes { arg env, factor;
	^env.times = env.times * factor;
	}
	
	*stutter { arg env, factor;
	env.times = env.times.stutter(factor);
	env.levels = env.levels.stutter(factor);
	^env;
	}
	
	*pyramid { arg env, factor;
	env.levels = env.levels.pyramid(factor);
	^env;
	}
	
	*shift { arg env, factor;
	env.levels = env.levels.shift(1, factor);
	^env;
	}
	
	*exprandTimes { arg env, min=0.01, max=10;
	env.times = env.times.exprand(env.times.size, min, max);
	^env;
	}
	
	*exprandLevels { arg env, min=0.01, max=10;
	env.levels = env.levels.exprand(env.levels.size, min, max);
	^env;
	}
	
	*sortTimes { arg env;
	env.times.sort;
	^env;
	}
	
	*getRandomByPoints {arg points, curve=1;
	var times, values; 
	
	values = Array.new(points);
	times = Array.new(points-1);
	
	points.do({
		values.add(rrand(0.01, 0.99));
	});
	
	(points - 1).do({
		times.add(rrand(0.01, 0.99));
	});
	BSequencer
	^Env.new(values, times, [curve]);
	
	}
	
	*getBetaByPoints {arg points, curve=1;
	var times, values, beta;
	beta = Pbeta(0.01, 0.99, 0.1, 0.1, inf); 
	values = Array.new(points);
	times = Array.new(points-1);
	
	points.do({
		values.add(beta.asStream.nextN(1)[0]);
	});
	
	(points - 1).do({
		times.add(beta.asStream.nextN(1)[0]);
	});
	
	^Env.new(values, times, [curve]);
	
	}
	
	*getBrownianByPoints {arg points, curve=1;
	var times, values, pattern;
	pattern = Pbrown(0.01, 0.99, 0.1, inf); 
	values = Array.new(points);
	times = Array.new(points-1);
	
	points.do({
		values.add(pattern.asStream.nextN(1)[0]);
	});
	
	(points - 1).do({
		times.add(pattern.asStream.nextN(1)[0]);
	});
	
	^Env.new(values, times, [curve]);
	
	}
	
	*getGeomBrownianByPoints {arg points, curve=1;
	var times, values, pattern;
	pattern = Pgbrown(0.01, 0.99, 0.05, inf); 
	values = Array.new(points);
	times = Array.new(points-1);
	
	points.do({
		values.add(pattern.asStream.nextN(1)[0]);
	});
	
	(points - 1).do({
		times.add(pattern.asStream.nextN(1)[0]);
	});
	
	^Env.new(values, times, [curve]);
	}
	
	*getLinearInByPoints {arg points, curve=1;	
	^Env.new([0.0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75,  1.0], [0.125, 0.125, 0.125, 0.125, 0.125, 0.125, 0.125], [curve]);
	}
	
	*getLinearOutByPoints {arg points, curve=1;	
	^Env.new([ 1, 0.75, 0.625, 0.5, 0.375, 0.25, 0.125, 0 ], [0.125, 0.125, 0.125, 0.125, 0.125, 0.125, 0.125], [curve]);
	}
	
	*getMiddleInByPoints {arg points, curve=1;	
	^Env.new([0.0, 0.125, 0.25, 0.75, 1.0, 0.625, 0.25, 0.0], [0.125, 0.125, 0.125, 0.125, 0.125, 0.125, 0.125], [curve]);
	}
	
	*getMiddleOutByPoints {arg points, curve=1;	
	^Env.new([1.0, 0.75, 0.25, 0.125, 0.0, 0.25, 0.625, 1.0], [0.125, 0.125, 0.125, 0.125, 0.125, 0.125, 0.125], [curve]);
	}
}