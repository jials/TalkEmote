/**
 * MFCC: Mel-Frequency Cepstrum Coefficient
 * ENERGY: Energy
 * MS: Magnitude Spectrum
 * ZCR: Zero Crossing Rate
 * MFCCENERGY: MFCC + Energy
 * MFCCMS: MFCC + MS
 * MFCCZCR: MFCC + ZCR
 * ENERGYMS: Energy + MS
 * ENERGYZCR: Energy + ZCR
 * MSZCR: MS + ZCR
 */
package evaluation;

public enum Feature {
	MFCC, ENERGY, MS, ZCR, 
	MFCCENERGY, MFCCMS, MFCCZCR, ENERGYMS, ENERGYZCR, MSZCR,
	MFCC_ENERGY_MS, MFCC_MS_ZCR, ENERGY_MS_ZCR, MFCC_ENERGY_ZCR,
	MFCC_ENERGY_MS_ZCR
};
