// Bootstrap Icons 1.13.1 (MIT): /vendor/bootstrap-icons/1.13.1/LICENSE
const CHEVRON_SVG = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="chevron bi bi-chevron-right" aria-hidden="true" focusable="false" viewBox="0 0 16 16">
  <path fill-rule="evenodd" d="M4.646 1.646a.5.5 0 0 1 .708 0l6 6a.5.5 0 0 1 0 .708l-6 6a.5.5 0 0 1-.708-.708L10.293 8 4.646 2.354a.5.5 0 0 1 0-.708"/>
</svg>`;
function el(tag, cls) {
    const n = document.createElement(tag);
    if (cls)
        n.className = cls;
    return n;
}
function isCurrentPageAttribute(el) {
    return el.hasAttribute("iscurrentpage") || el.hasAttribute("isCurrentPage");
}
export class SoBreadcrumbItem extends HTMLElement {
}
export class SoBreadcrumb extends HTMLElement {
    constructor() {
        super();
        this.observer = null;
        this.root = this.attachShadow({ mode: "open" });
    }
    connectedCallback() {
        this.render();
        this.observer = new MutationObserver(() => this.render());
        this.observer.observe(this, {
            childList: true,
            subtree: true,
            attributes: true,
            characterData: true
        });
    }
    disconnectedCallback() {
        this.observer?.disconnect();
        this.observer = null;
    }
    getItems() {
        const nodes = Array.from(this.querySelectorAll("so-breadcrumb-item"));
        const items = nodes
            .map((node) => {
            const label = (node.textContent ?? "").trim();
            return {
                label,
                href: node.getAttribute("href"),
                isCurrentPage: isCurrentPageAttribute(node)
            };
        })
            .filter((item) => item.label.length > 0);
        if (!items.length)
            return [];
        if (!items.some((item) => item.isCurrentPage)) {
            items[items.length - 1].isCurrentPage = true;
        }
        return items;
    }
    render() {
        this.root.innerHTML = "";
        const styleEl = document.createElement("style");
        styleEl.textContent = this.styles();
        this.root.appendChild(styleEl);
        const wrapper = el("nav", "so-breadcrumb");
        wrapper.setAttribute("aria-label", "Breadcrumb");
        const container = el("div", "so-breadcrumb-container");
        const list = el("ol", "list");
        const items = this.getItems();
        items.forEach((item, index) => {
            const isLast = index === items.length - 1;
            const li = el("li", "item");
            if (item.isCurrentPage)
                li.classList.add("current");
            if (item.isCurrentPage || !item.href) {
                const labelEl = el("span", "label");
                labelEl.textContent = item.label;
                if (item.isCurrentPage) {
                    labelEl.setAttribute("aria-current", "page");
                }
                li.appendChild(labelEl);
            }
            else {
                const labelEl = el("a");
                labelEl.href = item.href;
                labelEl.textContent = item.label;
                li.appendChild(labelEl);
            }
            if (!isLast) {
                li.insertAdjacentHTML("beforeend", CHEVRON_SVG);
            }
            list.appendChild(li);
        });
        container.appendChild(list);
        wrapper.appendChild(container);
        this.root.appendChild(wrapper);
    }
    styles() {
        return `
      :host{
        display: block;
        width: 100%;
        font-family: var(--so-font-family, Frutiger, sans-serif);
      }

      .so-breadcrumb{
        width: 100%;
        color: var(--so-fg, rgb(47, 72, 88));
      }

      .so-breadcrumb-container{
        padding: var(--so-breadcrumb-padding, 24px);
      }

      .list{
        list-style: none;
        display: flex;
        flex-wrap: wrap;
        align-items: flex-end;
        gap: 0.5rem;
        padding: 0;
        margin: 0;
        font-size: 14px;
        line-height: 1.4;
      }

      .item{
        display: inline-flex;
        align-items: flex-end;
        gap: 0.5rem;
        color: inherit;
      }

      .item a,
      .item .label{
        color: inherit;
        text-decoration: none;
        line-height: 1;
      }

      .item a:hover,
      .item a:focus-visible{
        color: #e01f26;
        text-decoration: none;
      }

      .item.current,
      .item.current .label,
      .item.current a{
        color: #e01f26;
      }

      .chevron{
        width: 16px;
        height: 16px;
        fill: #e01f26;
        flex: 0 0 auto;
        display: block;
      }
    `;
    }
}
