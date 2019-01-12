# -*- coding: utf-8 -*-

'''
Math extension for Python-Markdown
==================================
Adds support for displaying math formulas using
[MathJax](http://www.mathjax.org/).
Author: 2015-2017, Dmitry Shachnev <mitya57@gmail.com>.
'''

from markdown.inlinepatterns import Pattern
from markdown.extensions import Extension
from markdown.util import AtomicString, etree


class MathExtension(Extension):
    def __init__(self, *args, **kwargs):
        self.config = {
            'enable_dollar_delimiter':
                [False, 'Enable single-dollar delimiter'],
            'add_preview': [False, 'Add a preview node before each math node'],
            'use_asciimath':
                [False, 'Use AsciiMath syntax instead of TeX syntax'],
        }
        super(MathExtension, self).__init__(*args, **kwargs)

    def _get_content_type(self):
        if self.getConfig('use_asciimath'):
            return 'math/asciimath'
        return 'math/tex'

    def extendMarkdown(self, md):
        def _wrap_node(node, preview_text, wrapper_tag):
            if not self.getConfig('add_preview'):
                return node
            preview = etree.Element('span', {'class': 'MathJax_Preview'})
            preview.text = AtomicString(preview_text)
            wrapper = etree.Element(wrapper_tag)
            wrapper.extend([preview, node])
            return wrapper

        def handle_match_inline(m):
            node = etree.Element('script')
            node.set('type', self._get_content_type())
            node.text = AtomicString(m.group(3))
            return _wrap_node(node, ''.join(m.group(2, 3, 4)), 'span')

        def handle_match(m):
            node = etree.Element('script')
            node.set('type', '%s; mode=display' % self._get_content_type())
            if '\\begin' in m.group(2):
                node.text = AtomicString(''.join(m.group(2, 4, 5)))
                return _wrap_node(node, ''.join(m.group(1, 2, 4, 5, 6)), 'div')
            else:
                node.text = AtomicString(m.group(3))
                return _wrap_node(node, ''.join(m.group(2, 3, 4)), 'div')

        inlinemathpatterns = (
            Pattern(r'(?<!\\|\$)(\$)([^\$]+)(\$)'),   #  $...$
            Pattern(r'(?<!\\)(\\\()(.+?)(\\\))')      # \(...\)
        )
        mathpatterns = (
            Pattern(r'(?<!\\)(\$\$)([^\$]+)(\$\$)'),  # $$...$$
            Pattern(r'(?<!\\)(\\\[)(.+?)(\\\])'),     # \[...\]
            Pattern(r'(?<!\\)(\\begin{([a-z]+?\*?)})(.+?)(\\end{\3})')
        )
        if not self.getConfig('enable_dollar_delimiter'):
            inlinemathpatterns = inlinemathpatterns[1:]
        if self.getConfig('use_asciimath'):
            mathpatterns = mathpatterns[:-1]  # \begin...\end is TeX only
        for i, pattern in enumerate(mathpatterns):
            pattern.handleMatch = handle_match
            # we should have higher priority than 'escape' which has 180
            md.inlinePatterns.register(pattern, 'math-%d' % i, 185)
        for i, pattern in enumerate(inlinemathpatterns):
            pattern.handleMatch = handle_match_inline
            md.inlinePatterns.register(pattern, 'math-inline-%d' % i, 185)
        if self.getConfig('enable_dollar_delimiter'):
            md.ESCAPED_CHARS.append('$')


def makeExtension(*args, **kwargs):
    return MathExtension(*args, **kwargs)
