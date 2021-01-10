package alec_wam.enchantutils.common.util;

import java.math.BigInteger;
import java.math.RoundingMode;

import javax.annotation.Nonnull;

import com.google.common.math.BigIntegerMath;
import com.google.common.math.LongMath;

import net.minecraft.entity.player.PlayerEntity;

public class XPUtil {
	
	
	public static int getPlayerXP(@Nonnull PlayerEntity player) {
		try {
			return player.abilities.isCreativeMode ? Integer.MAX_VALUE / 2
					: Math.addExact(getExperienceForLevel(player.experienceLevel), (int) (player.experience * player.xpBarCap()));
		} catch (ArithmeticException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static long getPlayerXPL(@Nonnull PlayerEntity player) {
		return player.abilities.isCreativeMode ? Integer.MAX_VALUE / 2
				: Math.addExact(getExperienceForLevelL(player.experienceLevel), (long) (player.experience * player.xpBarCap()));
	}
	
	public static int getExperienceForLevel(int level) {
		if (level < 0) {
			throw new ArithmeticException("level underflow");
		}
		return Math.toIntExact(calculateXPfromLevel(level));
	}
	
	private static final int MAX_LEVEL_LONG = 1431655783;
	public static long getExperienceForLevelL(int level) {
		if (level < 0) {
			throw new ArithmeticException("level underflow");
		}
		if (level > MAX_LEVEL_LONG) {
			return Long.MAX_VALUE;
		}
		return calculateXPfromLevel(level);
	}
	
	/*
	 * The level has 3 ranges with different formula. For the highest range, we can use a neat reverse formula but that one would ignore the lower levels. So we
	 * calculate the offset from the real value beforehand using using the "not that good" formulas.
	 */
	private static final long LVLOFFSET32 = -calculateXPfromLevelHigh(32) + calculateXPfromLevelLow(32);

	private static long calculateXPfromLevel(int level) {
		if (level >= 32) {
			return calculateXPfromLevelHigh(level) + LVLOFFSET32;
		} else {
			return calculateXPfromLevelLow(level);
		}
	}
	
	public static long termial(long level) {
		return (level * level + level) / 2l;
	}

	private static long calculateXPfromLevelHigh(int level) {
		return -158L * (level + 1L) + termial(level - 1) * 9L; // correct in long, but offset by LVLOFFSET32
	}

	private static long calculateXPfromLevelLow(int level) {
		if (level >= 1 && level <= 16) {
			return (long) (Math.pow(level, 2) + 6 * level);
		} else if (level >= 17 && level <= 31) {
			return (long) (2.5 * Math.pow(level, 2) - 40.5 * level + 360);
		} else if (level >= 32) {
			return (long) (4.5 * Math.pow(level, 2) - 162.5 * level + 2220); // bad formula in long
		} else {
			return 0;
		}
	}
	
	public static int getLevelForExperience(long experience) {
		return getLevelFromExp(experience);
	}
	
	private static final BigInteger B72 = BigInteger.valueOf(72);
	private static final BigInteger B54215 = BigInteger.valueOf(54215);
	private static final BigInteger B325 = BigInteger.valueOf(325);
	private static final BigInteger B18 = BigInteger.valueOf(18);
	private static int getLevelFromExp(long exp) {
		if (exp > Long.MAX_VALUE / 72) {
			return BigIntegerMath.sqrt(BigInteger.valueOf(exp).multiply(B72).subtract(B54215), RoundingMode.DOWN).add(B325).divide(B18).intValueExact();
		}
		if (exp > Integer.MAX_VALUE) {
			return (int) ((LongMath.sqrt(72 * exp - 54215, RoundingMode.DOWN) + 325) / 18);
		}
		if (exp > 1395) {
			return (int) ((Math.sqrt(72 * exp - 54215) + 325) / 18);
		}
		if (exp > 315) {
			return (int) (Math.sqrt(40 * exp - 7839) / 10 + 8.1);
		}
		if (exp > 0) {
			return (int) (Math.sqrt(exp + 9) - 3);
		}
		return 0;
	}
	
	public static int getXpBarCapacity(int level) {
		if (level >= 30) {
			return -158 + level * 9;
		} else if (level >= 15) {
			return -38 + level * 5;
		} else if (level >= 0) {
			return 7 + level * 2;
		} else {
			throw new ArithmeticException("level underflow");
		}
	}
	
	public static void addPlayerXP(@Nonnull PlayerEntity player, int amount) {
		try {
			int experience = Math.max(0, Math.addExact(getPlayerXP(player), amount));
			player.experienceTotal = experience;
			player.experienceLevel = getLevelForExperience(experience);
			int expForLevel = getExperienceForLevel(player.experienceLevel);
			player.experience = (float) (experience - expForLevel) / (float) getXpBarCapacity(player.experienceLevel);
		} catch (ArithmeticException e) {
			e.printStackTrace();
		}
	}

	public static int limit(long l) {
		// assert(l > 0);
		return (l & 0xFFFFFFFF80000000L) != 0 ? Integer.MAX_VALUE : (int) l;
	}
	
	public static void addPlayerXP(@Nonnull PlayerEntity player, long amount) {
		try {
			long experience = Math.max(0, Math.addExact(getPlayerXPL(player), amount));
			player.experienceTotal = limit(experience);
			player.experienceLevel = getLevelForExperience(experience);
			long expForLevel = getExperienceForLevelL(player.experienceLevel);
			player.experience = (float) (experience - expForLevel) / (float) getXpBarCapacity(player.experienceLevel);
		} catch (ArithmeticException e) {
			e.printStackTrace();
		}
	}

}
