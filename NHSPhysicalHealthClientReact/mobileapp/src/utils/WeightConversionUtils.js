export const convertStonesAndPoundsToKgs = (stone, pounds) => {
  var poundsTotal = stone * 14 + pounds;
  var kgs = Math.round(poundsTotal / 2.20462);
  return kgs;
}

export const convertKgsToStonesAndPounds = (kgs) => {
  var poundTotal = kgs * 2.20462;
  var stone = Math.floor(poundTotal / 14);
  var pounds = Math.round(poundTotal - (stone * 14));
  return {
    stones: stone,
    pounds: pounds
  }
}

export const getPounds = (kgs) => {
  var poundTotal = kgs * 2.20462;
  var stone = Math.floor(poundTotal / 14);
  var pounds = Math.round(poundTotal - (stone * 14));
 
  return pounds;
}

export const getStones = (kgs) => {
  var poundTotal = kgs * 2.20462;
  var stone = Math.floor(poundTotal / 14);

  return stone;
}

export const getStonesDecimal = (kgs) => {
  var poundTotal = kgs * 2.20462;
  var stone = poundTotal / 14;

  return stone;
}

