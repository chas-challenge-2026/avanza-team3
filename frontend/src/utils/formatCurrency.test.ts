import { describe, it, expect } from "vitest";
import { formatCurrency } from "./formatCurrency";

describe("formatCurrency", () => {
  it("formats a whole number with kr suffix", () => {
    const result = formatCurrency(63840);
    expect(result).toContain("kr");
    expect(result).toContain("63");
    expect(result).toContain("840");
  });

  it("formats to a decimal number correctly", () => {
    const result = formatCurrency(161824.7);
    expect(result).toContain("kr");
    expect(result).toContain("161");
    expect(result).toContain("824");
  });
});
