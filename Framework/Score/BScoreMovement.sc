
BScoreMovement
{
	var <>paramName, <>env, <>start, <>gran;

	*new { |paramName, env, start, gran=0.1|
		^super.newCopyArgs(paramName, env, start, gran);
	}

	simpleMovement {|item|
		^{SimpleMovement({|x| var value=env.at(env.times.sum * x); item.setParamAndUpdate(paramName, value)}, env.times.sum, startTime:0, gran:gran)}
	}

	getDuration {
		^env.times.sum;
	}
}
