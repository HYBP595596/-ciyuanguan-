// 封面缺省占位：小说允许没有封面（url 为 null），前端用纯白替代
// 用 800x480 的白色 SVG，配合各处的 object-fit 能自然铺满容器
const WHITE_PLACEHOLDER =
    'data:image/svg+xml;charset=utf-8,' +
    encodeURIComponent(
        "<svg xmlns='http://www.w3.org/2000/svg' width='800' height='480'>" +
        "<rect width='800' height='480' fill='#ffffff'/></svg>"
    );

export function imageOrWhite(url) {
    if (url === null || url === undefined) return WHITE_PLACEHOLDER;
    if (typeof url !== 'string') return WHITE_PLACEHOLDER;
    return url.trim() === '' ? WHITE_PLACEHOLDER : url;
}

export default imageOrWhite;
