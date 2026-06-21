import { AxeBuilder } from '@axe-core/playwright';
import { expect, type Page } from '@playwright/test';

/**
 * 无障碍硬门禁 — WCAG 2.2 AA（W3C 标准），axe-core 实现。
 * 标签覆盖 WCAG 2.0/2.1/2.2 的 A + AA 规则集。
 */
export const WCAG_AA_TAGS = ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'];

export interface A11yOptions {
  /** 临时豁免的规则 id（应配 TODO，不可长期挂账） */
  disableRules?: string[];
  /** 仅扫描某区域的 CSS 选择器 */
  include?: string;
}

/**
 * 断言页面无 serious/critical 级 WCAG AA 违规。
 * 返回完整 axe 结果，便于在软门禁里记录 moderate/minor。
 */
export async function assertA11yClean(page: Page, opts: A11yOptions = {}) {
  let builder = new AxeBuilder({ page }).withTags(WCAG_AA_TAGS);
  if (opts.include) builder = builder.include(opts.include);
  if (opts.disableRules?.length) builder = builder.disableRules(opts.disableRules);

  const results = await builder.analyze();
  const blocking = results.violations.filter(
    (v) => v.impact === 'serious' || v.impact === 'critical',
  );
  const summary = blocking
    .map((v) => `  • [${v.impact}] ${v.id} ×${v.nodes.length} — ${v.help}`)
    .join('\n');
  expect(blocking, `WCAG 2.2 AA serious/critical 违规：\n${summary}`).toEqual([]);
  return results;
}

/** 仅采集违规计数（软门禁/报告用，不抛错）。 */
export async function scanA11y(page: Page, opts: A11yOptions = {}) {
  let builder = new AxeBuilder({ page }).withTags(WCAG_AA_TAGS);
  if (opts.include) builder = builder.include(opts.include);
  if (opts.disableRules?.length) builder = builder.disableRules(opts.disableRules);
  const r = await builder.analyze();
  const by = (i: string) => r.violations.filter((v) => v.impact === i).length;
  return { critical: by('critical'), serious: by('serious'), moderate: by('moderate'), minor: by('minor'), raw: r };
}
