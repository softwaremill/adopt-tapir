export class NodeAbsoluteLocation {
  private readonly slugs: string[];
  private readonly path: string;

  constructor(...slugs: string[]) {
    this.slugs = slugs;
    this.path = slugs.join('/');
  }

  public isChildOf(other: NodeAbsoluteLocation): boolean {
    return this.path.startsWith(other.path);
  }

  public isParentOf(other: NodeAbsoluteLocation): boolean {
    return other.path.startsWith(this.path);
  }

  public isSameAs(other: NodeAbsoluteLocation): boolean {
    return other.path === this.path;
  }

  public add(slug: string): NodeAbsoluteLocation {
    return new NodeAbsoluteLocation(...this.slugs, slug);
  }

  public getParent(): NodeAbsoluteLocation {
    if (this.isRoot()) {
      return this;
    }
    const parentSlugs = [...this.slugs.slice(0, this.slugs.length - 1)];
    return new NodeAbsoluteLocation(...parentSlugs);
  }

  public isRoot(): boolean {
    return this.slugs.length === 0;
  }

  public getLevel(): number {
    return this.slugs.length;
  }

  public getSlugs(): string[] {
    return [...this.slugs];
  }

  public getName(): string {
    if (this.isRoot()) {
      return '/';
    }
    return this.slugs.at(-1) as string;
  }
}

export const RootNodeLocation = new NodeAbsoluteLocation();
