export function circularOffset(options: {
    number: number;
    offset: number;
    min: number;
    max: number;
}): number {
    const size = options.max - options.min;

    const numberWOffset = options.number + options.offset;

    const offsetInSize = numberWOffset % size;

    return offsetInSize + options.min;
}
