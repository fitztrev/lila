import * as pagination from '../pagination';
import header from './header';
import teamInfo from './teamInfo';
import TournamentController from '../ctrl';
import { controls, standing } from './arena';
import { h, VNode } from 'snabbdom';
import { MaybeVNodes } from '../interfaces';
import { onInsert } from 'common/snabbdom';
import { teamStanding } from './battle';

export const name = 'created';

export function main(ctrl: TournamentController): MaybeVNodes {
  const pag = pagination.players(ctrl);
  return [
    header(ctrl),
    teamStanding(ctrl, 'created'),
    controls(ctrl, pag),
    standing(ctrl, pag, 'created'),
    h('blockquote.pull-quote', [h('p', ctrl.data.quote.text), h('footer', ctrl.data.quote.author)]),
    ctrl.opts.$faq
      ? h('div', {
          hook: onInsert(el => $(el).replaceWith(ctrl.opts.$faq)),
        })
      : null,
  ];
}

export const table = (ctrl: TournamentController): VNode | undefined =>
  ctrl.teamInfo.requested ? teamInfo(ctrl) : undefined;
